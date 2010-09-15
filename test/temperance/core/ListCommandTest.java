package temperance.core;

import java.util.HashMap;
import java.util.List;

import libmemcached.wrapper.type.BehaviorType;

import org.junit.Assert;
import org.junit.Test;

import temperance.exception.LockTimeoutException;
import temperance.exception.MemcachedOperationException;
import temperance.hash.Digest;
import temperance.hashing.MecabHashing;
import temperance.storage.TpList;
import temperance.storage.impl.MemcachedList;

public class ListCommandTest {
    
    @Test
    public void deleteAllValues_messy_patch() throws MemcachedOperationException, LockTimeoutException {
        Configure configure = new Configure();
        configure.setFullTextHashFunction(Digest.MD5);
        configure.setMemcached("localhost:11211");
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
        
        TpList list = new MemcachedList(pooling.getConnectionPool());
        list.add("key", "1", 10);
        list.add("key", "2", 10);
        list.add("key", "3", 10);
        list.add("key", "4", 10);
        list.add("key", "5", 10);
        list.add("key", "6", 10);
        list.add("key", "7", 10);
        list.add("key", "8", 10);
        
        {
            List<String> results = list.get("key", 0, 10);
            Assert.assertEquals(results.size(), 8);
            Assert.assertEquals(results.get(0), "1");
            Assert.assertEquals(results.get(1), "2");
            Assert.assertEquals(results.get(2), "3");
            Assert.assertEquals(results.get(3), "4");
            Assert.assertEquals(results.get(4), "5");
            Assert.assertEquals(results.get(5), "6");
            Assert.assertEquals(results.get(6), "7");
            Assert.assertEquals(results.get(7), "8");
        }
        
        ListCommand.DeleteAllValues d = new ListCommand.DeleteAllValues(pooling.getConnectionPool(), "key", 0, "5");
        
        Assert.assertTrue(d.apply());
        
        {
            List<String> results = list.get("key", 0, 10);
            Assert.assertEquals(results.size(), 7);
            Assert.assertEquals(results.get(0), "1");
            Assert.assertEquals(results.get(1), "2");
            Assert.assertEquals(results.get(2), "3");
            Assert.assertEquals(results.get(3), "4");
            Assert.assertEquals(results.get(4), "6");
            Assert.assertEquals(results.get(5), "7");
            Assert.assertEquals(results.get(6), "8");
        }
    }

}
