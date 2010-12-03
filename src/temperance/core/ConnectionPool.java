package temperance.core;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import libmemcached.wrapper.MemcachedBehavior;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.MemcachedServerList;
import libmemcached.wrapper.type.BehaviorType;
import libmemcached.wrapper.type.DistributionType;
import libmemcached.wrapper.type.ReturnType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import temperance.exception.InitializationException;
import temperance.lock.impl.RendezvousLock;

public class ConnectionPool implements LifeCycle {
    
    protected static final Log logger = LogFactory.getLog(ConnectionPool.class);
    
    protected final Configure configure;
    
    protected final int initialPoolSize;
    
    protected final int maxPoolSize;
    
    protected final BlockingQueue<MemcachedClient> pool;
    
    protected final MemcachedClient rootClient = new MemcachedClient();
    
    protected final AtomicInteger getConnections = new AtomicInteger(0);
    
    protected final RendezvousLock fillRequest = new RendezvousLock();
    
    protected final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
    
    protected final AtomicBoolean filledPool = new AtomicBoolean(false);
    
    protected final AtomicLong lastAccess = new AtomicLong(0L);
    
    protected final long keepAliveTime = TimeUnit.SECONDS.toMillis(1800L);
    
    protected final int fillingThreshold;
    
    public ConnectionPool(Configure configure){
        this.configure = configure;
        int initialPoolSize = configure.getInitialConnectionPoolSize();
        int maxPoolSize = configure.getMaxConnectionPoolSize();
        if(maxPoolSize < initialPoolSize){
            maxPoolSize = initialPoolSize;
        }
        
        this.initialPoolSize = initialPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.pool = new ArrayBlockingQueue<MemcachedClient>(maxPoolSize);
        this.fillingThreshold = (int) Math.round(maxPoolSize * 0.8);
    }
    
    protected static Map<BehaviorType, Boolean> createBehaviorTypeOption(Map<BehaviorType, Boolean> userValue){
        Map<BehaviorType, Boolean> behaviors = new EnumMap<BehaviorType, Boolean>(userValue);
        // default values
        if(!behaviors.containsKey(BehaviorType.SUPPORT_CAS)){
            behaviors.put(BehaviorType.SUPPORT_CAS, Boolean.TRUE);
        }
        if(!behaviors.containsKey(BehaviorType.BUFFER_REQUESTS)){
            behaviors.put(BehaviorType.BUFFER_REQUESTS, Boolean.TRUE);
        }
        if(!behaviors.containsKey(BehaviorType.NO_BLOCK)){
            behaviors.put(BehaviorType.NO_BLOCK, Boolean.TRUE);
        }
        if(!behaviors.containsKey(BehaviorType.TCP_KEEPALIVE)){
            behaviors.put(BehaviorType.TCP_KEEPALIVE, Boolean.TRUE);
        }
        if(!behaviors.containsKey(BehaviorType.TCP_NODELAY)){
            behaviors.put(BehaviorType.TCP_NODELAY, Boolean.TRUE);
        }
        if(!behaviors.containsKey(BehaviorType.BINARY_PROTOCOL)){
            behaviors.put(BehaviorType.BINARY_PROTOCOL, Boolean.FALSE);
        }
        return behaviors;
    }
    
    protected void setupMemcachedClient(MemcachedClient client){
        logger.info(new StringBuilder("confiugre: memcached serverlist: ").append(configure.getMemcached()));
        
        MemcachedServerList serverList = client.getServerList().parse(configure.getMemcached());
        try {
            ReturnType pushed = serverList.push();
            if(!ReturnType.SUCCESS.equals(pushed)){
                throw new InitializationException("server list failure: " + configure.getMemcached() + " was " + pushed);
            }
        } finally {
            serverList.free();
        }
        
        MemcachedBehavior behavior = client.getBehavior();
        behavior.setDistribution(DistributionType.CONSISTENT);
        behavior.set(BehaviorType.CONNECT_TIMEOUT, configure.getMemcachedConnectionTimeout());
        
        Map<BehaviorType, Boolean> userBehaviorType = configure.getPoolBehaviors();
        Map<BehaviorType, Boolean> behaviorTypeOption = createBehaviorTypeOption(userBehaviorType);
        Iterator<Map.Entry<BehaviorType, Boolean>> it = behaviorTypeOption.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<BehaviorType, Boolean> entry = it.next();
            BehaviorType key = entry.getKey();
            boolean value = entry.getValue().booleanValue();
            
            ReturnType beh = behavior.set(key, value);
            if(!ReturnType.SUCCESS.equals(beh)){
                throw new InitializationException("behavior set failure: " + entry + " was " + beh);
            }
            logger.info(new StringBuilder("configure: memcached behavior: ").append(key).append(":").append(value));
        }
    }
    
    public void init(){
        logger.info("init connection pool");
        
        setupMemcachedClient(rootClient);
        
        // size
        logger.info(new StringBuilder("configure: initial connection pool size: ").append(initialPoolSize));
        logger.info(new StringBuilder("configure: max connection pool size: ").append(maxPoolSize));
        logger.info(new StringBuilder("configure: filling threshold: ").append(fillingThreshold));
        logger.info(new StringBuilder("configure: pool keepalive time: ").append(keepAliveTime).append(" ").append(TimeUnit.MILLISECONDS));
        
        // fill connections
        for(int i = 0; i < initialPoolSize; ++i){
            pool.offer(cloneClient());
        }
        
        final MemcachedClient client = get();
        try {
            ReturnType rt = client.version();
            if(!ReturnType.SUCCESS.equals(rt)){
                throw new InitializationException("connection failure: " + rt);
            }
        } finally {
            release(client);
        }
        
        // fill pool: infinite
        executor.execute(new FillPoolTask());
        // free pool: every 10 sec
        executor.scheduleWithFixedDelay(new FreePoolTask(), 10, 10, TimeUnit.SECONDS);
        
        if(logger.isDebugEnabled()){
            logger.debug(new StringBuilder("configure: scheduled: fill pool"));
            logger.debug(new StringBuilder("configure: scheduled fixed delay: free pool: ").append(10).append(" ").append(TimeUnit.SECONDS));
        }
    }
    
    public void destroy(){
        logger.info("destroy pool");
        executor.shutdown();
        executor.shutdownNow();
        
        synchronized(rootClient){
            rootClient.free();
        }
        while(0 < pool.size()){
            MemcachedClient client = pool.poll();
            if(null != client){
                synchronized(client){
                    client.free();
                }
            }
        }
        
        logger.info("pool destroyed");
    }
    
    public MemcachedClient get(){
        lastAccess.set(System.currentTimeMillis());
        
        final int getConnectionCount = getConnections.incrementAndGet();
        if(fillingThreshold <= getConnectionCount || initialPoolSize <= getConnectionCount){
            final boolean filled = filledPool.get();
            if(!filled){
                fillRequest.release();
            }
        }
        
        try {
            final MemcachedClient client = pool.poll(5, TimeUnit.MILLISECONDS);
            if(null != client){
                return client;
            }
            return pool.take();
        } catch(InterruptedException e){
            throw new RuntimeException(e);
        }
    }
    
    public void release(MemcachedClient client){
        getConnections.decrementAndGet();
        
        synchronized(client){
            client.quit();
            client.free();
        }
        pool.offer(cloneClient());
    }
    
    protected MemcachedClient cloneClient(){
        try {
            synchronized(rootClient){
                return rootClient.clone();
            }
        } catch(CloneNotSupportedException e){
            throw new RuntimeException(e);
        }
    }
    
    protected class FillPoolTask implements Runnable {
        @Override
        public void run() {
            while(true){
                try {
                    fillRequest.await();
                    
                    for(int i = initialPoolSize; i < maxPoolSize; ++i){
                        pool.offer(cloneClient());
                    }
                    
                    filledPool.set(true);
                } catch (InterruptedException e) {
                    // nop
                }
            }
        }
    }
    
    protected class FreePoolTask implements Runnable {
        @Override
        public void run() {
            final long currentTimestamp = System.currentTimeMillis();
            final long lastAccessTimestamp = lastAccess.get();
            
            final long diff = currentTimestamp - lastAccessTimestamp;
            if(keepAliveTime < diff){
                if(logger.isDebugEnabled()){
                    logger.debug("freeing connection pool");
                }
                
                
                for(int i = initialPoolSize; i < maxPoolSize; ++i){
                    MemcachedClient client = pool.poll();
                    if(null != client){
                        getConnections.decrementAndGet();
                        synchronized(client){
                            client.quit();
                            client.free();
                        }
                    }
                }
                
                if(getConnections.get() < 1){
                    getConnections.set(initialPoolSize);
                }
                
                filledPool.set(false);
                lastAccess.set(System.currentTimeMillis());
            }
        }
    }
}
