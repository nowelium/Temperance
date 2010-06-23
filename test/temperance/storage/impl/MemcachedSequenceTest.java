package temperance.storage.impl;

import java.util.HashMap;
import java.util.List;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.type.BehaviorType;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import temperance.core.Configure;
import temperance.core.ConnectionPool;
import temperance.storage.Sequence.SequenceResult;

public class MemcachedSequenceTest {
    
    private static ConnectionPool pool;
    
    private MemcachedSequence seq;
    
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
        seq = new MemcachedSequence(pool);
    }
    
    @After
    public void cleanup(){
        pool.get().getStorage().flush(0);
    }

    @Test
    public void add() throws LibMemcachedException {
        Assert.assertEquals(seq.add("key-1", "value-1", 10), 0);
        Assert.assertEquals(seq.add("key-1", "value-2", 10), 1);
        Assert.assertEquals(seq.add("key-2", "value-3", 10), 0);
        
        Assert.assertEquals(seq.add("key-1", "value-4", 10), 2);
        Assert.assertEquals(seq.add("key-a", "value-5", 10), 0);
        Assert.assertEquals(seq.add("key-b", "value-6", 10), 0);
        
        Assert.assertEquals(seq.add("key-1", "value-7", 10), 3);
        Assert.assertEquals(seq.add("key-2", "value-8", 10), 1);
        
        // duplicate value
        Assert.assertEquals(seq.add("key-1", "value-1", 10), 4);
    }
    
    @Test
    public void get() throws LibMemcachedException {
        seq.add("key-1", "value-1", 10);
        seq.add("key-1", "value-2", 10);
        seq.add("key-2", "value-3", 10);
        {
            List<String> key1 = seq.get("key-1", 0, 2);
            System.out.println(key1);
            Assert.assertEquals(key1.size(), 2);
            Assert.assertEquals(key1.get(0), "value-1");
            Assert.assertEquals(key1.get(1), "value-2");
        }
        {
            List<String> key1 = seq.get("key-1", 0, 3);
            System.out.println(key1);
            Assert.assertEquals(key1.size(), 2);
            Assert.assertEquals(key1.get(0), "value-1");
            Assert.assertEquals(key1.get(1), "value-2");
        }
        {
            List<String> key2 = seq.get("key-2", 0, 2);
            System.out.println(key2);
            Assert.assertEquals(key2.size(), 1);
            Assert.assertEquals(key2.get(0), "value-3");
        }
        
        List<String> hoge = seq.get("hoge", 0, 1);
        Assert.assertEquals(hoge.size(), 0);
    }
    
    @Test
    public void get_offset_limit() throws LibMemcachedException {
        seq.add("key", "value-1", 10);
        seq.add("key", "value-2", 10);
        seq.add("key", "value-3", 10);
        seq.add("key", "value-4", 10);
        seq.add("key", "value-5", 10);
        
        {
            List<String> result = seq.get("key", 0, 2);
            System.out.println(result);
            Assert.assertEquals(result.size(), 2);
            Assert.assertEquals(result.get(0), "value-1");
            Assert.assertEquals(result.get(1), "value-2");
        }
        {
            List<String> result = seq.get("key", 1, 2);
            System.out.println(result);
            Assert.assertEquals(result.size(), 2);
            Assert.assertEquals(result.get(0), "value-2");
            Assert.assertEquals(result.get(1), "value-3");
        }
        {
            List<String> result = seq.get("key", 2, 1);
            System.out.println(result);
            Assert.assertEquals(result.size(), 1);
            Assert.assertEquals(result.get(0), "value-3");
        }
        {
            List<String> result = seq.get("key", 2, 2);
            System.out.println(result);
            Assert.assertEquals(result.size(), 2);
            Assert.assertEquals(result.get(0), "value-3");
            Assert.assertEquals(result.get(1), "value-4");
        }
        {
            List<String> result = seq.get("key", 1, 3);
            System.out.println(result);
            Assert.assertEquals(result.size(), 3);
            Assert.assertEquals(result.get(0), "value-2");
            Assert.assertEquals(result.get(1), "value-3");
            Assert.assertEquals(result.get(2), "value-4");
        }
    }
    
    @Test
    public void getByResult() throws LibMemcachedException {
        seq.add("key-a", "value-0", 10);
        seq.add("key-a", "value-1", 10);
        seq.add("key-a", "value-2", 10);
        
        List<SequenceResult> results = seq.getByResult("key-a", 0, 3);
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
    public void getAt() throws LibMemcachedException {
        Assert.assertEquals(seq.add("key", "value-1", 10), 0);
        Assert.assertEquals(seq.add("key", "value-2", 10), 1);
        Assert.assertEquals(seq.add("key", "value-3", 10), 2);
        Assert.assertEquals(seq.add("key", "value-4", 10), 3);
        Assert.assertEquals(seq.add("key", "value-5", 10), 4);
        
        Assert.assertEquals(seq.getAt("key", 0), "value-1");
        Assert.assertEquals(seq.getAt("key", 2), "value-3");
        Assert.assertEquals(seq.getAt("key", 0), "value-1");
        Assert.assertEquals(seq.getAt("key", 4), "value-5");
    }
    
    @Test
    public void delete() throws LibMemcachedException {
        seq.add("key", "value1", 10);
        seq.add("key", "value2", 10);
        seq.add("key", "value3", 10);
        seq.add("key", "value4", 10);
        
        {
            List<String> results = seq.get("key", 0, 4);
            System.out.println(results);
            Assert.assertEquals(results.size(), 4);
            Assert.assertEquals(results.get(0), "value1");
            Assert.assertEquals(results.get(3), "value4");
        }
        
        Assert.assertTrue(seq.delete("key", 0));
        
        {
            List<String> results = seq.get("key", 0, 4);
            System.out.println(results);
            Assert.assertEquals(results.size(), 0);
        }
    }
    
    @Test
    public void delete_notfound_key() throws LibMemcachedException {
        Assert.assertFalse(seq.delete("aaaaa", 0));
    }
    
    @Test
    public void deletAt() throws LibMemcachedException {
        seq.add("key", "value-1", 10);
        seq.add("key", "value-2", 10);
        seq.add("key", "value-3", 10);
        seq.add("key", "value-4", 10);
        seq.add("key", "value-5", 10);
        
        Assert.assertEquals(seq.getAt("key", 0), "value-1");
        Assert.assertEquals(seq.count("key"), 5);
        
        Assert.assertTrue(seq.deleteAt("key", 0, 0));
        Assert.assertNull(seq.getAt("key", 0));
        // XXX: fix count size(use reindex)
        Assert.assertEquals(seq.count("key"), 5);
        
        Assert.assertEquals(seq.getAt("key", 2), "value-3");
        Assert.assertTrue(seq.deleteAt("key", 2, 0));
        Assert.assertNull(seq.getAt("key", 2));
        // XXX: fix count size(use reindex)
        Assert.assertEquals(seq.count("key"), 5);
    }
    
    @Test
    public void count() throws LibMemcachedException {
        Assert.assertEquals(seq.count("keey"), 0);
        seq.add("keey", "value", 10);
        Assert.assertEquals(seq.count("keey"), 1);
        seq.add("keey", "value", 10);
        Assert.assertEquals(seq.count("keey"), 2);
        seq.add("keey", "value", 10);
        Assert.assertEquals(seq.count("keey"), 3);
        
        List<String> results = seq.get("keey", 0, 10);
        Assert.assertEquals(results.size(), 3);
    }

    @Test
    public void reindex() throws LibMemcachedException {
        seq.add("key", "value-1", 10);
        seq.add("key", "value-2", 10);
        seq.add("key", "value-3", 10);
        seq.add("key", "value-4", 10);
        seq.add("key", "value-5", 10);
        
        Assert.assertEquals(seq.getAt("key", 0), "value-1");
        Assert.assertEquals(seq.count("key"), 5);
        
        Assert.assertTrue(seq.deleteAt("key", 0, 0));
        Assert.assertNull(seq.getAt("key", 0));
        Assert.assertEquals(seq.count("key"), 5);
        
        seq.reindex("key");
        Assert.assertEquals(seq.count("key"), 4);
    }
    
    @Test
    public void reindex_large_index() throws LibMemcachedException {
        for(int i = 0; i < 50000; ++i){
            seq.add("key", Integer.toString(i), 0);
        }
        
        Assert.assertEquals(seq.count("key"), 50000);
        
        seq.deleteAt("key", 2, 0);
        seq.deleteAt("key", 20, 0);
        seq.deleteAt("key", 200, 0);
        seq.deleteAt("key", 2000, 0);
        seq.deleteAt("key", 20000, 0);
        
        Assert.assertEquals(seq.count("key"), 50000);
        
        seq.reindex("key");
        
        Assert.assertEquals(seq.count("key"), 49995);
    }
}
