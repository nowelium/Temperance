package temperance.memcached;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import libmemcached.exception.LibMemcachedException;
import libmemcached.exception.MaximumPoolException;
import libmemcached.wrapper.MemcachedBehavior;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.MemcachedPool;
import libmemcached.wrapper.type.BehaviorType;
import libmemcached.wrapper.type.DistributionType;
import temperance.handler.Context;
import temperance.lock.impl.CountDownLock;

public class Pool {
    
    protected static final int INITIAL_POOL_SIZE = 100;
    
    protected final Context context;
    
    protected final int maxPoolSize;
    
    protected final AtomicReference<MemcachedPool> refPool = new AtomicReference<MemcachedPool>();
    
    protected final MemcachedClient rootClient;
    
    protected final CountDownLock lock;
    
    protected final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public Pool(Context context){
        this.context = context;
        int maxPoolSize = context.getMemcachedPoolSize();
        if(maxPoolSize < INITIAL_POOL_SIZE){
            maxPoolSize = INITIAL_POOL_SIZE;
        }
        this.maxPoolSize = maxPoolSize;
        
        MemcachedClient client = new MemcachedClient();
        client.getServerList().parse(context.getMemcached()).push();
        MemcachedBehavior behavior = client.getBehavior();
        behavior.setDistribution(DistributionType.CONSISTENT);
        behavior.set(BehaviorType.SUPPORT_CAS, true);
        behavior.set(BehaviorType.BUFFER_REQUESTS, true);
        behavior.set(BehaviorType.TCP_KEEPALIVE, true);
        behavior.set(BehaviorType.TCP_NODELAY, true);
        
        this.rootClient = client;
        
        this.lock = new CountDownLock((int) Math.round(maxPoolSize * 0.8));
    }
    
    protected void resetPool(){
        MemcachedPool pool = rootClient.createPool(INITIAL_POOL_SIZE, maxPoolSize);
        pool.setBehavior(BehaviorType.DISTRIBUTION, DistributionType.CONSISTENT);
        pool.setBehavior(BehaviorType.SUPPORT_CAS, true);
        pool.setBehavior(BehaviorType.BUFFER_REQUESTS, true);
        pool.setBehavior(BehaviorType.TCP_KEEPALIVE, true);
        pool.setBehavior(BehaviorType.TCP_NODELAY, true);
        this.refPool.set(pool);
    }
    
    public void init(){
        resetPool();
        
        executor.execute(new Runnable(){
            public void run(){
                try {
                    // infinite rendezvous
                    while(true){
                        // rendezvous
                        lock.await();
                        
                        resetPool();
                    }
                } catch(InterruptedException e){
                    //
                }
            }
        });
        
        MemcachedClient client = get();
        try {
            client.version();
        } finally {
            release(client);
        }
    }
    
    public MemcachedClient get(){
        try {
            lock.countDown();
            
            return refPool.get().pop(false);
        } catch(MaximumPoolException e){
            // temporary connection
            return rootClient;
        } catch(LibMemcachedException e){
            throw new RuntimeException(e);
        }
    }
    
    public void release(MemcachedClient client){
        if(client == rootClient){
            // non pool release: root client
            return;
        }
        
        // TODO: reuse pool
        client.quit();
    }

}
