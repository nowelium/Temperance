package temperance.memcached;

import java.util.concurrent.atomic.AtomicReference;

import libmemcached.exception.LibMemcachedException;
import libmemcached.exception.MaximumPoolException;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.MemcachedPool;
import libmemcached.wrapper.type.BehaviorType;
import libmemcached.wrapper.type.DistributionType;
import temperance.handler.Context;

public class Pool {
    
    protected final Context context;
    
    protected final int maxPoolSize;
    
    protected final AtomicReference<MemcachedPool> refPool = new AtomicReference<MemcachedPool>();
    
    public Pool(Context context){
        this.context = context;
        int poolSize = context.getMemcachedPoolSize();
        if(poolSize < 1){
            poolSize = 1;
        }
        this.maxPoolSize = poolSize;
    }
    
    public void init(){
        MemcachedClient client = new MemcachedClient();
        client.getServerList().parse(context.getMemcached()).push();
        
        MemcachedPool pool = client.createPool(1, maxPoolSize);
        pool.setBehavior(BehaviorType.DISTRIBUTION, DistributionType.CONSISTENT.getValue());
        
        refPool.set(pool);
    }
    
    public Connection get(){
        try {
            return new Connection(refPool.get().pop(false));
        } catch(MaximumPoolException e){
            // FIXME: reuse pool connection
            synchronized(this){
                init();
            }
            
            try {
                return new Connection(refPool.get().pop(true));
            } catch(Exception ex){
                throw new RuntimeException(e);
            }
        } catch(LibMemcachedException e){
            throw new RuntimeException(e);
        }
    }

}
