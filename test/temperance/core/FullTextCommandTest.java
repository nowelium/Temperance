package temperance.core;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import libmemcached.wrapper.type.BehaviorType;

import org.junit.Assert;
import org.junit.Test;

import temperance.exception.LockTimeoutException;
import temperance.exception.MemcachedOperationException;
import temperance.hash.Digest;
import temperance.hash.StringHash;
import temperance.hashing.MecabHashing;
import temperance.storage.TpFullText;
import temperance.storage.impl.MemcachedFullText;

public class FullTextCommandTest {
    
    @Test
    public void deleteAllValues_single() throws MemcachedOperationException, LockTimeoutException, InterruptedException {
        Configure configure = new Configure();
        configure.setFullTextHashFunction(Digest.MD5);
        configure.setMemcached("localhost:12121");
        configure.setNodeFilter(MecabHashing.Filter.Nouns);
        configure.setPoolBehaviors(new HashMap<BehaviorType, Boolean>(){
            private static final long serialVersionUID = 1L;
            {
                put(BehaviorType.BUFFER_REQUESTS, Boolean.FALSE);
            }
        });
        configure.setMaxConnectionPoolSize(1000);
        
        Pooling pooling = new Pooling(configure);
        pooling.init();
        
        try {
            FullTextCommand cmd = new FullTextCommand(pooling);
            
            final String key = Long.toString(System.currentTimeMillis());
            TpFullText ft = new MemcachedFullText(pooling.getConnectionPool());
            for(int i = 0; i < 1000; ++i){
                ft.add(key, new StringHash(Integer.toString(i)), "hello world", 1000);
            }
            
            System.out.println("delete all values");
            
            // before delete
            Assert.assertEquals(ft.hashCountByValue(key, "hello world"), 1000);
            
            cmd.deleteAllValues(key, 0, "hello world");
            
            // in-progress: delete all values
            Assert.assertEquals(ft.hashCountByValue(key, "hello world"), 1000);
            
            TimeUnit.SECONDS.sleep(10);
            
            Assert.assertEquals(ft.hashCountByValue(key, "hello world"), 1000);
            
            TimeUnit.SECONDS.sleep(100);
            
            // done: delete all values 
            Assert.assertEquals(ft.hashCountByValue(key, "hello world"), 0);
        } finally {
            pooling.getConnectionPool().get().getStorage().flush(0);
            pooling.destroy();
        }
    }

    @Test
    public void deleteAllValues_multi() throws MemcachedOperationException, LockTimeoutException, InterruptedException {
        final Configure configure = new Configure();
        configure.setFullTextHashFunction(Digest.MD5);
        configure.setMemcached("localhost:12121,localhost:12122,localhost:12123,localhost:12124,localhost:12125");
        configure.setNodeFilter(MecabHashing.Filter.Nouns);
        configure.setPoolBehaviors(new HashMap<BehaviorType, Boolean>(){
            private static final long serialVersionUID = 1L;
            {
                put(BehaviorType.BUFFER_REQUESTS, Boolean.TRUE);
            }
        });
        configure.setMaxConnectionPoolSize(10000);
        
        final Pooling pooling = new Pooling(configure);
        pooling.init();
        
        try {
            FullTextCommand cmd = new FullTextCommand(pooling);
            
            final String key = Long.toString(System.currentTimeMillis());
            
            TpFullText ft = new MemcachedFullText(pooling.getConnectionPool());
            for(int i = 0; i < 1000; ++i){
                ft.add(key, new StringHash(key + Integer.toString(i)), "hello world", 1000);
            }
            
            System.out.println("delete all values");
            
            // before delete
            Assert.assertEquals(ft.hashCountByValue(key, "hello world"), 1000);
            
            cmd.deleteAllValues(key, 0, "hello world");
            
            // in-progress: delete all values
            Assert.assertEquals(ft.hashCountByValue(key, "hello world"), 1000);
            
            TimeUnit.SECONDS.sleep(10);
            
            Assert.assertEquals(ft.hashCountByValue(key, "hello world"), 1000);
            
            TimeUnit.SECONDS.sleep(50);
            
            // done: delete all values 
            Assert.assertEquals(ft.hashCountByValue(key, "hello world"), 0);
        } finally {
            pooling.getConnectionPool().get().getStorage().flush(0);
            pooling.destroy();
        }
    }
    
}
