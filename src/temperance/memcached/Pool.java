package temperance.memcached;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import libmemcached.exception.LibMemcachedException;
import libmemcached.exception.MaximumPoolException;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.MemcachedPool;
import libmemcached.wrapper.type.BehaviorType;
import libmemcached.wrapper.type.DistributionType;
import temperance.handler.Context;

public class Pool {
    
    protected final Context context;
    
    protected final ReferenceQueue<MemcachedClient> connectionQueue = new ReferenceQueue<MemcachedClient>();
    
    protected final MemcachedPool pool;
    
    protected final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public Pool(Context context){
        this.context = context;
    
        MemcachedClient client = new MemcachedClient();
        client.getServerList().parse(context.getMemcached()).push();
        
        int maxPoolSize = context.getMemcachedPoolSize();
        if(maxPoolSize < 1){
            maxPoolSize = 1;
        }
        pool = client.createPool(1, maxPoolSize);
        pool.setBehavior(BehaviorType.DISTRIBUTION, DistributionType.CONSISTENT.getValue());
    }
    
    public void init(){
        executor.execute(new Runnable(){
            public void run(){
                try {
                    while(true){
                        System.out.println("hoge");
                        Reference<? extends MemcachedClient> refRemovedClient = connectionQueue.remove();
                        System.out.println(refRemovedClient);
                        MemcachedClient removedClient = refRemovedClient.get();
                        pool.push(removedClient);
                    }
                } catch(InterruptedException e){
                    //
                }
            }
        });
    }
    
    public Connection get(){
        try {
            return new Connection(pool.pop(true), connectionQueue);
        } catch(MaximumPoolException e){
            throw new RuntimeException(e);
        } catch(LibMemcachedException e){
            throw new RuntimeException(e);
        }
    }

}
