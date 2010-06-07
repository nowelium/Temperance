package temperance.memcached;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import libmemcached.wrapper.MemcachedBehavior;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.type.BehaviorType;
import libmemcached.wrapper.type.DistributionType;
import temperance.lock.impl.CountDownLock;
import temperance.rpc.Context;

public class ConnectionPool {
    
    protected static final int INITIAL_POOL_SIZE = 100;
    
    protected final Context context;
    
    protected final int maxPoolSize;
    
    protected final MemcachedClient rootClient;
    
    protected final ExecutorService executor = Executors.newCachedThreadPool();
    
    protected final BlockingQueue<MemcachedClient> pool;
    
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
    
    public void init(){
        for(int i = 0; i < INITIAL_POOL_SIZE; ++i){
            pool.offer(new MemcachedClient(rootClient));
        }
        
        executor.execute(new Runnable(){
            public void run(){
                try {
                    // infinite loop
                    while(true){
                        // rendezvous
                        lock.await();
                        
                        int capacity = pool.remainingCapacity();
                        int count = capacity - INITIAL_POOL_SIZE;
                        for(int i = 0; i < count; ++i){
                            if(!pool.offer(new MemcachedClient(rootClient))){
                                break;
                            }
                        }
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
        client.quit();
        pool.offer(new MemcachedClient(client));
    }

}
