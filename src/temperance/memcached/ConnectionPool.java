package temperance.memcached;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import libmemcached.wrapper.MemcachedBehavior;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.type.BehaviorType;
import libmemcached.wrapper.type.DistributionType;
import libmemcached.wrapper.type.ReturnType;
import temperance.exception.InitializationException;
import temperance.lock.impl.CountDownLock;
import temperance.rpc.Context;

public class ConnectionPool {
    
    protected static final int INITIAL_POOL_SIZE = 30;
    
    protected final Context context;
    
    protected final int maxPoolSize;
    
    protected final MemcachedClient client;
    
    protected final ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
    
    protected final BlockingQueue<MemcachedClient> pool;
    
    protected final BlockingQueue<MemcachedClient> release = new LinkedBlockingQueue<MemcachedClient>();
    
    protected final AtomicBoolean fillPool = new AtomicBoolean(false);
    
    protected final AtomicLong lastAccess = new AtomicLong(0L);
    
    protected final long keepAliveTime = TimeUnit.SECONDS.toMillis(60);
    
    protected final CountDownLock lock;
    
    public ConnectionPool(Context context){
        this.context = context;
        int maxPoolSize = context.getMemcachedPoolSize();
        if(maxPoolSize < INITIAL_POOL_SIZE){
            maxPoolSize = INITIAL_POOL_SIZE;
        }
        this.maxPoolSize = maxPoolSize;
        this.pool = new LinkedBlockingQueue<MemcachedClient>(maxPoolSize);
        this.client = new MemcachedClient();
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
        if(!behaviors.containsKey(BehaviorType.SUPPORT_CAS)){
            behaviors.put(BehaviorType.SUPPORT_CAS, Boolean.TRUE);
        }
        if(!behaviors.containsKey(BehaviorType.BUFFER_REQUESTS)){
            behaviors.put(BehaviorType.BUFFER_REQUESTS, Boolean.TRUE);
        }
        if(!behaviors.containsKey(BehaviorType.TCP_KEEPALIVE)){
            behaviors.put(BehaviorType.TCP_KEEPALIVE, Boolean.TRUE);
        }
        if(!behaviors.containsKey(BehaviorType.TCP_NODELAY)){
            behaviors.put(BehaviorType.TCP_NODELAY, Boolean.TRUE);
        }
        return behaviors;
    }
    
    public void init(){
        ReturnType pushed = client.getServerList().parse(context.getMemcached()).push();
        if(!ReturnType.SUCCESS.equals(pushed)){
            throw new InitializationException("server list failure: " + context.getMemcached() + " was " + pushed);
        }
        
        MemcachedBehavior behavior = client.getBehavior();
        behavior.setDistribution(DistributionType.CONSISTENT);
        
        Map<BehaviorType, Boolean> userBehaviorType = context.getPoolBehaviors();
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
        }
        
        for(int i = 0; i < INITIAL_POOL_SIZE; ++i){
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
        // free pool: every 10 sec
        executor.scheduleWithFixedDelay(new FreePoolTask(), 10, 10, TimeUnit.SECONDS);
        // release pool: every 5000 usec
        // TODO: 5000 usec await when: libmemcached becomes segfault by excessive access
        executor.scheduleWithFixedDelay(new ReleasePoolTask(), 0, 5000, TimeUnit.MICROSECONDS);
    }
    
    public MemcachedClient get(){
        lastAccess.set(System.currentTimeMillis());
        
        try {
            lock.countDown();
            
            MemcachedClient connection = pool.poll(10, TimeUnit.MILLISECONDS);
            if(null != connection){
                return connection;
            }
            return pool.take();
        } catch(InterruptedException e){
            e.printStackTrace();
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
                    
                    int capacity = pool.remainingCapacity();
                    int count = capacity - INITIAL_POOL_SIZE;
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
                while(INITIAL_POOL_SIZE < pool.size()){
                    pool.poll();
                }
                fillPool.set(false);
            }
        }
    }

    protected class ReleasePoolTask implements Runnable {
        public void run(){
            MemcachedClient client = release.poll();
            if(null == client){
                return;
            }
            
            client.quit();
            pool.offer(ConnectionPool.this.clone());
        }
    }
}
