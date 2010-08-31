package temperance.core;

import java.util.HashMap;

import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.type.BehaviorType;

import org.junit.Test;

import temperance.hash.Digest;
import temperance.hashing.MecabHashing;


public class ConnectionPoolTest {
    
    @Test
    public void get(){
        Configure configure = new Configure();
        configure.setFullTextHashFunction(Digest.MD5);
        configure.setMemcached("demo:12121");
        configure.setNodeFilter(MecabHashing.Filter.Nouns);
        configure.setPoolBehaviors(new HashMap<BehaviorType, Boolean>(){
            private static final long serialVersionUID = 1L;
            {
                put(BehaviorType.BUFFER_REQUESTS, Boolean.FALSE);
            }
        });
        configure.setMaxConnectionPoolSize(200);
        
        Pooling pooling = new Pooling(configure);
        pooling.init();
        
        ConnectionPool pool = pooling.getConnectionPool();
        for(int i = 0; i < 5000; ++i){
            MemcachedClient c = pool.get();
            System.out.println(i);
            pool.release(c);
        }
        
        pooling.destroy();
    }

}
