package temperance.storage.impl;

import java.util.HashMap;
import java.util.List;

import libmemcached.wrapper.type.BehaviorType;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import temperance.core.Configure;
import temperance.core.ConnectionPool;
import temperance.exception.LockTimeoutException;
import temperance.exception.MemcachedOperationException;
import temperance.storage.TpList.SequenceResult;

public class MemcachedListTest {
    
    private static ConnectionPool pool;
    
    private MemcachedList lst;
    
    @BeforeClass
    public static void setupPool(){
        Configure configure = new Configure();
        configure.setMemcached("localhost:11211");
        configure.setPoolBehaviors(new HashMap<BehaviorType, Boolean>(){
            private static final long serialVersionUID = 1L;
            {
                put(BehaviorType.BUFFER_REQUESTS, Boolean.FALSE);
            }
        });
        pool = new ConnectionPool(configure);
        pool.init();
    }
    
    @AfterClass
    public static void cleanupPool(){
        //pool.destroy();
    }
    
    @Before
    public void setup(){
        lst = new MemcachedList(pool);
    }
    
    @After
    public void cleanup(){
        pool.get().getStorage().flush(0);
    }

    @Test
    public void add() throws MemcachedOperationException, LockTimeoutException {
        Assert.assertEquals(lst.add("key-1", "value-1", 10), 0);
        Assert.assertEquals(lst.add("key-1", "value-2", 10), 1);
        Assert.assertEquals(lst.add("key-2", "value-3", 10), 0);
        
        Assert.assertEquals(lst.add("key-1", "value-4", 10), 2);
        Assert.assertEquals(lst.add("key-a", "value-5", 10), 0);
        Assert.assertEquals(lst.add("key-b", "value-6", 10), 0);
        
        Assert.assertEquals(lst.add("key-1", "value-7", 10), 3);
        Assert.assertEquals(lst.add("key-2", "value-8", 10), 1);
        
        // duplicate value
        Assert.assertEquals(lst.add("key-1", "value-1", 10), 4);
    }
    
    @Test
    public void get() throws MemcachedOperationException, LockTimeoutException {
        lst.add("key-1", "value-1", 10);
        lst.add("key-1", "value-2", 10);
        lst.add("key-2", "value-3", 10);
        {
            List<String> key1 = lst.get("key-1", 0, 2);
            System.out.println(key1);
            Assert.assertEquals(key1.size(), 2);
            Assert.assertEquals(key1.get(0), "value-1");
            Assert.assertEquals(key1.get(1), "value-2");
        }
        {
            List<String> key1 = lst.get("key-1", 0, 3);
            System.out.println(key1);
            Assert.assertEquals(key1.size(), 2);
            Assert.assertEquals(key1.get(0), "value-1");
            Assert.assertEquals(key1.get(1), "value-2");
        }
        {
            List<String> key2 = lst.get("key-2", 0, 2);
            System.out.println(key2);
            Assert.assertEquals(key2.size(), 1);
            Assert.assertEquals(key2.get(0), "value-3");
        }
        
        List<String> hoge = lst.get("hoge", 0, 1);
        Assert.assertEquals(hoge.size(), 0);
    }
    
    @Test
    public void get_offset_limit() throws MemcachedOperationException, LockTimeoutException {
        lst.add("key", "value-1", 10);
        lst.add("key", "value-2", 10);
        lst.add("key", "value-3", 10);
        lst.add("key", "value-4", 10);
        lst.add("key", "value-5", 10);
        
        {
            List<String> result = lst.get("key", 0, 2);
            System.out.println(result);
            Assert.assertEquals(result.size(), 2);
            Assert.assertEquals(result.get(0), "value-1");
            Assert.assertEquals(result.get(1), "value-2");
        }
        {
            List<String> result = lst.get("key", 1, 2);
            System.out.println(result);
            Assert.assertEquals(result.size(), 2);
            Assert.assertEquals(result.get(0), "value-2");
            Assert.assertEquals(result.get(1), "value-3");
        }
        {
            List<String> result = lst.get("key", 2, 1);
            System.out.println(result);
            Assert.assertEquals(result.size(), 1);
            Assert.assertEquals(result.get(0), "value-3");
        }
        {
            List<String> result = lst.get("key", 2, 2);
            System.out.println(result);
            Assert.assertEquals(result.size(), 2);
            Assert.assertEquals(result.get(0), "value-3");
            Assert.assertEquals(result.get(1), "value-4");
        }
        {
            List<String> result = lst.get("key", 1, 3);
            System.out.println(result);
            Assert.assertEquals(result.size(), 3);
            Assert.assertEquals(result.get(0), "value-2");
            Assert.assertEquals(result.get(1), "value-3");
            Assert.assertEquals(result.get(2), "value-4");
        }
    }
    
    @Test
    public void getByResult() throws MemcachedOperationException, LockTimeoutException {
        lst.add("key-a", "value-0", 10);
        lst.add("key-a", "value-1", 10);
        lst.add("key-a", "value-2", 10);
        
        List<SequenceResult> results = lst.getByResult("key-a", 0, 3);
        System.out.println(results);
        {
            SequenceResult result = results.get(0);
            Assert.assertEquals(result.getKey(), "key-a");
            Assert.assertEquals(result.getIndex(), 0);
            Assert.assertEquals(result.getValue(), "value-0");
        }
        {
            SequenceResult result = results.get(1);
            Assert.assertEquals(result.getKey(), "key-a");
            Assert.assertEquals(result.getIndex(), 1);
            Assert.assertEquals(result.getValue(), "value-1");
        }
        {
            SequenceResult result = results.get(2);
            Assert.assertEquals(result.getKey(), "key-a");
            Assert.assertEquals(result.getIndex(), 2);
            Assert.assertEquals(result.getValue(), "value-2");
        }
    }
    
    @Test
    public void getAt() throws MemcachedOperationException, LockTimeoutException {
        Assert.assertEquals(lst.add("key", "value-1", 10), 0);
        Assert.assertEquals(lst.add("key", "value-2", 10), 1);
        Assert.assertEquals(lst.add("key", "value-3", 10), 2);
        Assert.assertEquals(lst.add("key", "value-4", 10), 3);
        Assert.assertEquals(lst.add("key", "value-5", 10), 4);
        
        Assert.assertEquals(lst.getAt("key", 0), "value-1");
        Assert.assertEquals(lst.getAt("key", 2), "value-3");
        Assert.assertEquals(lst.getAt("key", 0), "value-1");
        Assert.assertEquals(lst.getAt("key", 4), "value-5");
    }
    
    @Test
    public void delete() throws MemcachedOperationException, LockTimeoutException {
        lst.add("key", "value1", 10);
        lst.add("key", "value2", 10);
        lst.add("key", "value3", 10);
        lst.add("key", "value4", 10);
        
        {
            List<String> results = lst.get("key", 0, 4);
            System.out.println(results);
            Assert.assertEquals(results.size(), 4);
            Assert.assertEquals(results.get(0), "value1");
            Assert.assertEquals(results.get(3), "value4");
        }
        
        Assert.assertTrue(lst.delete("key", 0));
        
        {
            List<String> results = lst.get("key", 0, 4);
            System.out.println(results);
            Assert.assertEquals(results.size(), 0);
        }
    }
    
    @Test
    public void delete_notfound_key() throws MemcachedOperationException, LockTimeoutException {
        Assert.assertFalse(lst.delete("aaaaa", 0));
    }
    
    @Test
    public void deletAt() throws MemcachedOperationException, LockTimeoutException {
        lst.add("key", "value-1", 10);
        lst.add("key", "value-2", 10);
        lst.add("key", "value-3", 10);
        lst.add("key", "value-4", 10);
        lst.add("key", "value-5", 10);
        
        Assert.assertEquals(lst.getAt("key", 0), "value-1");
        Assert.assertEquals(lst.count("key"), 5);
        
        Assert.assertTrue(lst.deleteAt("key", 0, 0));
        Assert.assertNull(lst.getAt("key", 0));
        // XXX: fix count size(use reindex)
        Assert.assertEquals(lst.count("key"), 5);
        
        Assert.assertEquals(lst.getAt("key", 2), "value-3");
        Assert.assertTrue(lst.deleteAt("key", 2, 0));
        Assert.assertNull(lst.getAt("key", 2));
        // XXX: fix count size(use reindex)
        Assert.assertEquals(lst.count("key"), 5);
    }
    
    @Test
    public void count() throws MemcachedOperationException, LockTimeoutException {
        Assert.assertEquals(lst.count("keey"), 0);
        lst.add("keey", "value", 10);
        Assert.assertEquals(lst.count("keey"), 1);
        lst.add("keey", "value", 10);
        Assert.assertEquals(lst.count("keey"), 2);
        lst.add("keey", "value", 10);
        Assert.assertEquals(lst.count("keey"), 3);
        
        List<String> results = lst.get("keey", 0, 10);
        Assert.assertEquals(results.size(), 3);
    }

    @Test
    public void reindex() throws MemcachedOperationException, LockTimeoutException {
        lst.add("key", "value-1", 10);
        lst.add("key", "value-2", 10);
        lst.add("key", "value-3", 10);
        lst.add("key", "value-4", 10);
        lst.add("key", "value-5", 10);
        
        Assert.assertEquals(lst.getAt("key", 0), "value-1");
        Assert.assertEquals(lst.count("key"), 5);
        
        Assert.assertTrue(lst.deleteAt("key", 0, 0));
        Assert.assertNull(lst.getAt("key", 0));
        Assert.assertEquals(lst.count("key"), 5);
        
        lst.reindex("key");
        Assert.assertEquals(lst.count("key"), 4);
    }
    
    @Test
    public void reindex_large_index() throws MemcachedOperationException, LockTimeoutException {
        for(int i = 0; i < 50000; ++i){
            lst.add("key", Integer.toString(i), 0);
        }
        
        Assert.assertEquals(lst.count("key"), 50000);
        
        lst.deleteAt("key", 2, 0);
        lst.deleteAt("key", 20, 0);
        lst.deleteAt("key", 200, 0);
        lst.deleteAt("key", 2000, 0);
        lst.deleteAt("key", 20000, 0);
        
        Assert.assertEquals(lst.count("key"), 50000);
        
        lst.reindex("key");
        
        Assert.assertEquals(lst.count("key"), 49995);
    }
}
