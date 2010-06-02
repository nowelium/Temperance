package temperance.memcached;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import libmemcached.exception.LibMemcachedException;
import libmemcached.exception.MaximumPoolException;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.MemcachedPool;
import libmemcached.wrapper.type.BehaviorType;
import libmemcached.wrapper.type.DistributionType;
import temperance.handler.Context;
import temperance.lock.impl.CountDownLock;

public class Pool {
    
    protected static final int INITIAL_POOL_SIZE = 30;
    
    protected final Context context;
    
    protected final int maxPoolSize;
    
    protected final AtomicReference<MemcachedPool> refPool = new AtomicReference<MemcachedPool>();
    
    protected final MemcachedClient rootClient;
    
    protected final CountDownLock latch;
    
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
        this.rootClient = client;
        
        this.latch = new CountDownLock((int) Math.round(maxPoolSize * 0.8));
    }
    
    protected void resetPool(){
        MemcachedPool pool = rootClient.createPool(INITIAL_POOL_SIZE, maxPoolSize);
        pool.setBehavior(BehaviorType.DISTRIBUTION, DistributionType.CONSISTENT.getValue());
        pool.setBehavior(BehaviorType.CACHE_LOOKUPS, 1);
        pool.setBehavior(BehaviorType.TCP_KEEPALIVE, 1);
        
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
                        latch.await();
                        
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
            latch.countDown();
            
            return refPool.get().pop(false);
        } catch(MaximumPoolException e){
            // 
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
