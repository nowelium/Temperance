package temperance.core;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
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
    
    protected static final long poolFillInterval;
    
    protected static final long poolReleaseInterval;
    
    static {
        String fillInterval = System.getProperty("temperance.memc.pool_fill_interval", "500");
        long fill = 500L;
        try {
            fill = Long.parseLong(fillInterval);
        } catch(NumberFormatException e){
            // nop
        }
        poolFillInterval = fill;
        
        String releaseInterval = System.getProperty("temperance.memc.pool_release_interval", "500");
        long release = 500L;
        try {
            release = Long.parseLong(releaseInterval);
        } catch(NumberFormatException e){
            // nop
        }
        poolReleaseInterval = release;
    }

    protected final Configure configure;
    
    protected final int initialPoolSize;
    
    protected final int maxPoolSize;
    
    protected final MemcachedClient client = new MemcachedClient();
    
    protected final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
    
    protected final List<ScheduledFuture<?>> schedules = new ArrayList<ScheduledFuture<?>>();
    
    protected final BlockingQueue<MemcachedClient> pool;
    
    protected final DelayQueue<ReleaseRequest> releaseRequestQueue = new DelayQueue<ReleaseRequest>();
    
    protected final DelayQueue<CreateRequest> fillRequestQueue = new DelayQueue<CreateRequest>();
    
    protected final AtomicBoolean poolFilled = new AtomicBoolean(false);
    
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
        this.lock = new CountDownLock((int) Math.round(maxPoolSize * 0.8));
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
    
    public void init(){
        logger.info("init connection pool");
        
        logger.info(new StringBuilder("confiugre: memcached serverlist: ").append(configure.getMemcached()));
        
        ReturnType pushed = client.getServerList().parse(configure.getMemcached()).push();
        if(!ReturnType.SUCCESS.equals(pushed)){
            throw new InitializationException("server list failure: " + configure.getMemcached() + " was " + pushed);
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
        
        // size
        logger.info(new StringBuilder("configure: initial connection pool size: ").append(initialPoolSize));
        logger.info(new StringBuilder("configure: max connection pool size: ").append(maxPoolSize));
        logger.info(new StringBuilder("configure: filling threshold: ").append(lock));
        logger.info(new StringBuilder("configure: pool keepalive time: ").append(keepAliveTime).append(" ").append(TimeUnit.MILLISECONDS));
        
        // intervals
        logger.info(new StringBuilder("configure: pool fill interval: ").append(poolFillInterval).append(" ").append(TimeUnit.MICROSECONDS));
        logger.info(new StringBuilder("configure: pool release interval: ").append(poolReleaseInterval).append(" ").append(TimeUnit.MICROSECONDS));
        
        // fill connections
        for(int i = 0; i < initialPoolSize; ++i){
            pool.offer(cloneClient());
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
        
        // fill pool now: wait
        executor.execute(new CreatePoolTask());
        // fill pool: infinite
        executor.execute(new FillPoolTask());
        // release pool: infinite
        executor.execute(new ReleasePoolTask());
        // free pool: every 10 sec
        schedules.add(executor.scheduleWithFixedDelay(new FreePoolTask(), 10, 10, TimeUnit.SECONDS));
        
        if(logger.isDebugEnabled()){
            logger.debug(new StringBuilder("configure: scheduled: fill pool now"));
            logger.debug(new StringBuilder("configure: scheduled: fill pool"));
            logger.debug(new StringBuilder("configure: scheduled: release pool"));
            logger.debug(new StringBuilder("configure: scheduled fixed delay: free pool: ").append(10).append(" ").append(TimeUnit.SECONDS));
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
            lock.release();
            
            MemcachedClient connection = pool.poll(5, TimeUnit.MILLISECONDS);
            if(null != connection){
                return connection;
            }
            
            // create request: now!
            fillRequest(100, TimeUnit.MICROSECONDS);
            
            return pool.take();
        } catch(InterruptedException e){
            throw new RuntimeException(e);
        }
    }
    
    public void release(MemcachedClient client){
        // TODO: 20000 usec await when: libmemcached becomes segfault by excessive access
        releaseRequestQueue.offer(new ReleaseRequest(client, poolReleaseInterval, TimeUnit.MICROSECONDS));
    }
    
    protected MemcachedClient cloneClient(){
        try {
            return client.clone();
        } catch(CloneNotSupportedException e){
            throw new RuntimeException(e);
        }
    }
    
    protected void fillRequest(long expire, TimeUnit unit){
        if(poolFilled.get()){
            return ;
        }
        
        final int capacity = pool.remainingCapacity();
        final int count = capacity - initialPoolSize;
        final int requestSize = fillRequestQueue.size();
        if(requestSize < capacity && requestSize < count && 0 < count){
            fillRequestQueue.offer(new CreateRequest(expire, unit));
        }
    }
    
    protected class CreatePoolTask implements Runnable {
        public void run(){
            try {
                while(true){
                    fillRequestQueue.take();
                    
                    if(!pool.offer(cloneClient())){
                        fillRequestQueue.clear();
                        
                        if(logger.isDebugEnabled()){
                            logger.debug("pool max; clear fill requests");
                        }
                    }
                }
            } catch(InterruptedException e){
                //
            }
        }
    }
    
    protected class FillPoolTask implements Runnable {
        public void run(){
            try {
                // infinite loop
                while(true){
                    // rendezvous
                    lock.await();
                    
                    if(poolFilled.get()){
                        continue;
                    }
                    
                    int capacity = pool.remainingCapacity();
                    int count = capacity - initialPoolSize;
                    for(int i = 0; i < count; ++i){
                        // TODO: await: libmemcached becaomes "CONNECTION SOCKET CREATE FAILURE" by excessive access
                        fillRequest(poolFillInterval, TimeUnit.MICROSECONDS);
                    }

                    poolFilled.set(true);
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
                
                poolFilled.set(false);
                lastAccess.set(System.currentTimeMillis());
            }
        }
    }

    protected class ReleasePoolTask implements Runnable {
        public void run(){
            try {
                while(true){
                    ReleaseRequest req = releaseRequestQueue.take();
                    req.getClient().quit();
                    
                    fillRequestQueue.offer(new CreateRequest(poolFillInterval, TimeUnit.MICROSECONDS));
                }
            } catch(InterruptedException e){
                // nop
            }
        }
    }
    
    protected static class CreateRequest implements Delayed {
        
        protected final long entryTime;
        
        protected final long expire;
        
        protected CreateRequest(long duration, TimeUnit unit){
            this.entryTime = System.nanoTime();
            this.expire = entryTime + unit.toNanos(duration);
        }

        public long getDelay(TimeUnit unit) {
            final long now = System.nanoTime();
            // expiredの時間から経過時間を引き、残り時間を算出
            return unit.convert(expire - now, TimeUnit.NANOSECONDS);
        }

        public int compareTo(Delayed o) {
            CreateRequest target = (CreateRequest) o;
            final long e = expire;
            final long t = target.expire;
            if(e < t){
                return -1;
            }
            if(e > t){
                return 1;
            }
            return 0;
        }
    }
    
    protected static class ReleaseRequest extends CreateRequest {
        
        protected final MemcachedClient client;

        protected ReleaseRequest(MemcachedClient client, long duration, TimeUnit unit) {
            super(duration, unit);
            this.client = client;
        }
        public MemcachedClient getClient(){
            return client;
        }
    }
}
