package temperance.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import libmemcached.wrapper.MemcachedBehavior;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.type.BehaviorType;
import libmemcached.wrapper.type.DistributionType;
import libmemcached.wrapper.type.ReturnType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import temperance.exception.InitializationException;
import temperance.lock.impl.CountDownLock;

public class ConnectionPool implements LifeCycle {
    
    protected static final Log logger = LogFactory.getLog(ConnectionPool.class);

    protected final Configure configure;
    
    protected final int initialPoolSize;
    
    protected final int maxPoolSize;
    
    protected final MemcachedClient client = new MemcachedClient();
    
    protected final ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
    
    protected final List<ScheduledFuture<?>> schedules = new ArrayList<ScheduledFuture<?>>();
    
    protected final BlockingQueue<MemcachedClient> pool;
    
    protected final BlockingQueue<MemcachedClient> release = new LinkedBlockingQueue<MemcachedClient>();
    
    protected final AtomicBoolean fillPool = new AtomicBoolean(false);
    
    protected final AtomicLong lastAccess = new AtomicLong(0L);
    
    protected final long keepAliveTime = TimeUnit.SECONDS.toMillis(1800L);
    
    protected final CountDownLock lock;
    
    public ConnectionPool(Configure configure){
        this.configure = configure;
        int initialPoolSize = configure.getInitialConnectionPoolSize();
        int maxPoolSize = configure.getMaxConnectionPoolSize();
        if(maxPoolSize < initialPoolSize){
            maxPoolSize = initialPoolSize;
        }
        
        this.initialPoolSize = initialPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.pool = new LinkedBlockingQueue<MemcachedClient>(maxPoolSize);
        this.lock = new CountDownLock((int) Math.round(maxPoolSize * 0.9));
    }
    
    protected MemcachedClient clone(){
        try {
            return client.clone();
        } catch(CloneNotSupportedException e){
            throw new RuntimeException(e);
        }
    }
    
    protected static Map<BehaviorType, Boolean> createBehaviorTypeOption(Map<BehaviorType, Boolean> userValue){
        Map<BehaviorType, Boolean> behaviors = new HashMap<BehaviorType, Boolean>(userValue);
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
    
    public void init(){
        logger.info("init connection pool");
        
        logger.info(new StringBuilder("confiugre: memcached serverlist: ").append(configure.getMemcached()));
        
        ReturnType pushed = client.getServerList().parse(configure.getMemcached()).push();
        if(!ReturnType.SUCCESS.equals(pushed)){
            throw new InitializationException("server list failure: " + configure.getMemcached() + " was " + pushed);
        }
        
        MemcachedBehavior behavior = client.getBehavior();
        behavior.setDistribution(DistributionType.CONSISTENT);
        
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
        
        logger.info(new StringBuilder("configure: initial connection pool size: ").append(initialPoolSize));
        logger.info(new StringBuilder("configure: max connection pool size: ").append(maxPoolSize));
        logger.info(new StringBuilder("configure: filling threshold: ").append(lock));
        logger.info(new StringBuilder("configure: pool keepalive time: ")
            .append(1800L).append(" ").append(TimeUnit.SECONDS)
        );
        
        // fill connections
        for(int i = 0; i < initialPoolSize; ++i){
            pool.offer(clone());
        }
        
        MemcachedClient client = get();
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
        // release pool: infinite
        executor.execute(new ReleasePoolTask());
        // free pool: every 10 sec
        schedules.add(executor.scheduleWithFixedDelay(new FreePoolTask(), 10, 10, TimeUnit.SECONDS));
        
        if(logger.isDebugEnabled()){
            logger.debug(new StringBuilder("configure: scheduled: fill pool"));
            logger.debug(new StringBuilder("configure: scheduled: release pool"));
            logger.debug(new StringBuilder("configure: scheduled fixed delay: free pool: ")
                .append(10).append(" ").append(TimeUnit.SECONDS)
            );
        }
    }
    
    public void destroy(){
        logger.info("destroy pool");
        for(ScheduledFuture<?> schedule: schedules){
            schedule.cancel(false);
        }
        executor.shutdown();
        
        logger.info("pool destroyed");
    }
    
    public MemcachedClient get(){
        lastAccess.set(System.currentTimeMillis());
        
        try {
            lock.countDown();
            
            MemcachedClient connection = pool.poll(5, TimeUnit.MILLISECONDS);
            if(null != connection){
                return connection;
            }
            return pool.take();
        } catch(InterruptedException e){
            throw new RuntimeException(e);
        }
    }
    
    public void release(MemcachedClient client){
        release.offer(client);
    }
    
    protected class FillPoolTask implements Runnable {
        public void run(){
            try {
                // infinite loop
                while(true){
                    // rendezvous
                    lock.await();
                    
                    if(fillPool.get()){
                        continue;
                    }

                    if(logger.isDebugEnabled()){
                        logger.debug("filling connection pool");
                    }
                    
                    int capacity = pool.remainingCapacity();
                    int count = capacity - initialPoolSize;
                    for(int i = 0; i < count; ++i){
                        if(!pool.offer(ConnectionPool.this.clone())){
                            break;
                        }
                    }
                    fillPool.set(true);
                }
            } catch(InterruptedException e){
                //
            }
        }
    }
    
    protected class FreePoolTask implements Runnable {
        public void run(){
            final long current = System.currentTimeMillis();
            final long last = lastAccess.get();
            if(keepAliveTime < (current - last)){
                if(logger.isDebugEnabled()){
                    logger.debug("freeing connection pool");
                }
                
                while(initialPoolSize < pool.size()){
                    MemcachedClient client = pool.poll();
                    if(null != client){
                        client.quit();
                    }
                }
                fillPool.set(false);
                lastAccess.set(System.currentTimeMillis());
            }
        }
    }

    protected class ReleasePoolTask implements Runnable {
        public void run(){
            try {
                while(true){
                    MemcachedClient client = release.take();
                    
                    client.quit();
                    
                    // TODO: 20000 usec await when: libmemcached becomes segfault by excessive access
                    TimeUnit.MICROSECONDS.sleep(20000);
                    
                    pool.offer(ConnectionPool.this.clone());
                }
            } catch(InterruptedException e){
                // nop
            }
        }
    }
}
