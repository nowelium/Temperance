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
import temperance.hash.Hash;
import temperance.hash.StringHash;
import temperance.util.Lists;


public class MemcachedFullTextTest {

private static ConnectionPool pool;
    
    private MemcachedFullText ft;
    
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
        ft = new MemcachedFullText(pool);
    }
    
    @After
    public void cleanup(){
        pool.get().getStorage().flush(0);
    }
    
    @Test
    public void add() throws MemcachedOperationException, LockTimeoutException {
        // new key, hash
        Assert.assertEquals(ft.add("key", new StringHash("hash-a"), "value-1", 10), 0);
        // exists key, hash
        Assert.assertEquals(ft.add("key", new StringHash("hash-a"), "value-2", 10), 1);
        // exists key, hash
        Assert.assertEquals(ft.add("key", new StringHash("hash-a"), "value-3", 10), 2);
        
        // exists key, new hash
        Assert.assertEquals(ft.add("key", new StringHash("hash-b"), "value-4", 10), 0);
        // exists key, new hash
        Assert.assertEquals(ft.add("key", new StringHash("hash-c"), "value-5", 10), 0);
        // exists key, hash
        Assert.assertEquals(ft.add("key", new StringHash("hash-a"), "value-6", 10), 3);
        
        // dup key, hash
        Assert.assertEquals(ft.add("key", new StringHash("hash-a"), "value-1", 10), 4);
        
        // new key, exists hash
        Assert.assertEquals(ft.add("key2", new StringHash("hash-a"), "value-7", 10), 0);
        Assert.assertEquals(ft.add("key2", new StringHash("hash-b"), "value-8", 10), 0);
    }
    
    @Test
    public void addAll() throws MemcachedOperationException, LockTimeoutException {
        List<Hash> hashes1 = Lists.newArrayList();
        hashes1.add(new StringHash("hash-a"));
        hashes1.add(new StringHash("hash-b"));
        hashes1.add(new StringHash("hash-c"));
        
        List<Long> ids1 = ft.addAll("key", hashes1, "value", 10);
        Assert.assertEquals(ids1.size(), 3);
        Assert.assertEquals(ids1.get(0), 0);
        Assert.assertEquals(ids1.get(1), 0);
        Assert.assertEquals(ids1.get(2), 0);

        // 
        List<Hash> hashes2 = Lists.newArrayList();
        hashes2.add(new StringHash("hash-a"));
        hashes2.add(new StringHash("hash-b"));
        hashes2.add(new StringHash("hash-c"));
        
        List<Long> ids2 = ft.addAll("key", hashes2, "value", 10);
        Assert.assertEquals(ids2.size(), 3);
        Assert.assertEquals(ids2.get(0), 1);
        Assert.assertEquals(ids2.get(1), 1);
        Assert.assertEquals(ids2.get(2), 1);

    }
}
