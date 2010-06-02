package temperance.memcached;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import libmemcached.exception.LibMemcachedException;
import libmemcached.exception.MaximumPoolException;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.MemcachedPool;
import libmemcached.wrapper.type.BehaviorType;
import libmemcached.wrapper.type.DistributionType;
import temperance.handler.Context;
import temperance.lock.impl.RendezvousLock;

public class Pool {
    
    protected final Context context;
    
    protected final MemcachedPool pool;
    
    protected final MemcachedClient rootClient;
    
    protected final RendezvousLock rendezvous = new RendezvousLock();
    
    protected final BlockingQueue<MemcachedClient> releaseQueue = new LinkedBlockingQueue<MemcachedClient>();
    
    protected final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public Pool(Context context){
        this.context = context;
        int maxPoolSize = context.getMemcachedPoolSize();
        if(maxPoolSize < 30){
            maxPoolSize = 30;
        }
        
        MemcachedClient client = new MemcachedClient();
        client.getServerList().parse(context.getMemcached()).push();
        this.rootClient = client;
        
        MemcachedPool pool = client.createPool(30, maxPoolSize);
        pool.setBehavior(BehaviorType.DISTRIBUTION, DistributionType.CONSISTENT.getValue());
        pool.setBehavior(BehaviorType.CACHE_LOOKUPS, 1);
        pool.setBehavior(BehaviorType.TCP_KEEPALIVE, 128);
        
        this.pool = pool;
    }
    
    public void init(){
        executor.execute(new Runnable(){
            public void run(){
                try {
                    // infinite rendezvous
                    while(true){
                        rendezvous.await();
                        
                        while(!releaseQueue.isEmpty()) {
                            push(releaseQueue.take());
                            
                            // FIXME: pool.push are hangup when ..excess.. access
                            TimeUnit.MILLISECONDS.sleep(100);
                        }
                    }
                } catch(InterruptedException e){
                    //
                }
            }
            protected void push(MemcachedClient client){
                System.out.println(client);
                pool.push(client);
            }
        });
        
        MemcachedClient client = get();
        try {
            System.out.println(client.version());
        } finally {
            release(client);
        }
    }
    
    public MemcachedClient get(){
        try {
            return pool.pop(false);
        } catch(MaximumPoolException e){
            // rendezvous
            rendezvous.release();
            
            // root client
            return rootClient;
        } catch(LibMemcachedException e){
            throw new RuntimeException(e);
        }
    }
    
    public void release(MemcachedClient client){
        if(rootClient == client){
            // non pool release: root client
            return;
        }
        
        releaseQueue.offer(client);
    }

}
