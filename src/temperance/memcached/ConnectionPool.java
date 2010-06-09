package temperance.memcached;

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
import temperance.lock.impl.CountDownLock;
import temperance.rpc.Context;

public class ConnectionPool {
    
    protected static final int INITIAL_POOL_SIZE = 30;
    
    protected final Context context;
    
    protected final int maxPoolSize;
    
    protected final MemcachedClient rootClient;
    
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
        
        MemcachedClient client = new MemcachedClient();
        client.getServerList().parse(context.getMemcached()).push();
        
        MemcachedBehavior behavior = client.getBehavior();
        behavior.setDistribution(DistributionType.CONSISTENT);
        behavior.set(BehaviorType.SUPPORT_CAS, true);
        behavior.set(BehaviorType.BUFFER_REQUESTS, true);
        behavior.set(BehaviorType.TCP_KEEPALIVE, true);
        //behavior.set(BehaviorType.TCP_NODELAY, true);
        
        this.rootClient = client;
        this.lock = new CountDownLock((int) Math.round(maxPoolSize * 0.9));
    }
    
    protected MemcachedClient clone(){
        try {
            return rootClient.clone();
        } catch(CloneNotSupportedException e){
            throw new RuntimeException(e);
        }
    }
    
    public void init(){
        for(int i = 0; i < INITIAL_POOL_SIZE; ++i){
            pool.offer(clone());
        }
        
        // fill pool
        executor.execute(new Runnable() {
            public void run(){
                try {
                    // infinite loop
                    while(true){
                        // rendezvous
                        lock.await();
                        
                        if(fillPool.get()){
                            continue;
                        }
                        
                        // TODO: max pool size
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
        });
        // reduce pool
        executor.scheduleWithFixedDelay(new Runnable() {
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
        }, 10, 10, TimeUnit.SECONDS);
        // reuse pool
        executor.scheduleWithFixedDelay(new Runnable() {
            public void run(){
                MemcachedClient client = release.poll();
                if(null == client){
                    return;
                }
                
                client.quit();
                pool.offer(ConnectionPool.this.clone());
            }
        }, 0, 5000, TimeUnit.MICROSECONDS); // TODO: await when: libmemcached becomes segfault by excessive access
        
        MemcachedClient client = get();
        try {
            ReturnType rt = client.version();
            if(!ReturnType.SUCCESS.equals(rt)){
                throw new RuntimeException("connection failure");
            }
        } finally {
            release(client);
        }
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

}
