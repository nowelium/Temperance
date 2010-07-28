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
import temperance.storage.TpFullText;
import temperance.storage.TpList.TpListResult;
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
        Assert.assertEquals(ids1.get(0).longValue(), 0);
        Assert.assertEquals(ids1.get(1).longValue(), 0);
        Assert.assertEquals(ids1.get(2).longValue(), 0);

        // 
        List<Hash> hashes2 = Lists.newArrayList();
        hashes2.add(new StringHash("hash-a"));
        hashes2.add(new StringHash("hash-b"));
        hashes2.add(new StringHash("hash-c"));
        
        List<Long> ids2 = ft.addAll("key", hashes2, "value", 10);
        Assert.assertEquals(ids2.size(), 3);
        Assert.assertEquals(ids2.get(0).longValue(), 1);
        Assert.assertEquals(ids2.get(1).longValue(), 1);
        Assert.assertEquals(ids2.get(2).longValue(), 1);
    }
    
    @Test
    public void getHashes() throws MemcachedOperationException, LockTimeoutException {
        ft.add("key", new StringHash("hash-a"), "value-a", 10);
        ft.add("key", new StringHash("hash-b"), "value-b", 10);
        ft.add("key", new StringHash("hash-c"), "value-c", 10);
        
        {
            List<Hash> hashes = ft.getHashes("key", 0, 3);
            System.out.println(hashes);
            Assert.assertEquals(hashes.size(), 3);
            Assert.assertEquals(hashes.get(0).hashValue(), "hash-a");
            Assert.assertEquals(hashes.get(1).hashValue(), "hash-b");
            Assert.assertEquals(hashes.get(2).hashValue(), "hash-c");
        }

        {
            List<Hash> hashes = ft.getHashes("key", 0, 30);
            System.out.println(hashes);
            Assert.assertEquals(hashes.size(), 3);
            Assert.assertEquals(hashes.get(0).hashValue(), "hash-a");
            Assert.assertEquals(hashes.get(1).hashValue(), "hash-b");
            Assert.assertEquals(hashes.get(2).hashValue(), "hash-c");
        }
        
        {
            List<Hash> hashes = ft.getHashes("key", 1, 2);
            System.out.println(hashes);
            Assert.assertEquals(hashes.size(), 2);
            Assert.assertEquals(hashes.get(0).hashValue(), "hash-b");
            Assert.assertEquals(hashes.get(1).hashValue(), "hash-c");
        }
        
        // nosuch key
        List<Hash> hashes = ft.getHashes("key00000", 0, 3);
        System.out.println(hashes);
        Assert.assertEquals(hashes.size(), 0);
    }
    
    @Test
    public void getHashes_exists() throws MemcachedOperationException, LockTimeoutException {
        //
        // TODO: to unique getHashes?
        //
        
        ft.add("key", new StringHash("hash-a"), "value-a", 10);
        ft.add("key", new StringHash("hash-a"), "value-b", 10);
        ft.add("key", new StringHash("hash-a"), "value-c", 10);
        
        {
            List<Hash> hashes = ft.getHashes("key", 0, 3);
            System.out.println(hashes);
            Assert.assertEquals(hashes.size(), 3);
            Assert.assertEquals(hashes.get(0).hashValue(), "hash-a");
            Assert.assertEquals(hashes.get(1).hashValue(), "hash-a");
            Assert.assertEquals(hashes.get(2).hashValue(), "hash-a");
        }
    }
    
    @Test
    public void getHashesByResult() throws MemcachedOperationException, LockTimeoutException {
        ft.add("key", new StringHash("hash-a"), "value-a", 10);
        ft.add("key", new StringHash("hash-b"), "value-b", 10);
        ft.add("key", new StringHash("hash-c"), "value-c", 10);
        
        {
            List<TpListResult> results = ft.getHashesByResult("key", 0, 3);
            System.out.println(results);
            {
                TpListResult result = results.get(0);
                Assert.assertEquals(result.getKey(), "key");
                Assert.assertEquals(result.getIndex(), 0);
                Assert.assertEquals(result.getValue(), "hash-a");
            }
            {
                TpListResult result = results.get(1);
                Assert.assertEquals(result.getKey(), "key");
                Assert.assertEquals(result.getIndex(), 1);
                Assert.assertEquals(result.getValue(), "hash-b");
            }
            {
                TpListResult result = results.get(2);
                Assert.assertEquals(result.getKey(), "key");
                Assert.assertEquals(result.getIndex(), 2);
                Assert.assertEquals(result.getValue(), "hash-c");
            }
        }
        
        {
            List<TpListResult> results = ft.getHashesByResult("key", 1, 2);
            System.out.println(results);
            {
                TpListResult result = results.get(0);
                Assert.assertEquals(result.getKey(), "key");
                Assert.assertEquals(result.getIndex(), 1);
                Assert.assertEquals(result.getValue(), "hash-b");
            }
            {
                TpListResult result = results.get(1);
                Assert.assertEquals(result.getKey(), "key");
                Assert.assertEquals(result.getIndex(), 2);
                Assert.assertEquals(result.getValue(), "hash-c");
            }
        }

        {
            List<TpListResult> results = ft.getHashesByResult("key", 0, 30);
            System.out.println(results);
            Assert.assertEquals(results.size(), 3);
        }

        
        List<TpListResult> rs = ft.getHashesByResult("key999999", 0, 30);
        System.out.println(rs);
        Assert.assertEquals(rs.size(), 0);
    }
    
    @Test
    public void getHashesByResult_exists() throws MemcachedOperationException, LockTimeoutException {
        ft.add("key", new StringHash("hash-a"), "value-a", 10);
        ft.add("key", new StringHash("hash-a"), "value-b", 10);
        ft.add("key", new StringHash("hash-a"), "value-c", 10);
        
        {
            List<TpListResult> results = ft.getHashesByResult("key", 0, 3);
            System.out.println(results);
            {
                TpListResult result = results.get(0);
                Assert.assertEquals(result.getKey(), "key");
                Assert.assertEquals(result.getIndex(), 0);
                Assert.assertEquals(result.getValue(), "hash-a");
            }
            {
                TpListResult result = results.get(1);
                Assert.assertEquals(result.getKey(), "key");
                Assert.assertEquals(result.getIndex(), 1);
                Assert.assertEquals(result.getValue(), "hash-a");
            }
            {
                TpListResult result = results.get(2);
                Assert.assertEquals(result.getKey(), "key");
                Assert.assertEquals(result.getIndex(), 2);
                Assert.assertEquals(result.getValue(), "hash-a");
            }
        }
    }
    
    @Test
    public void getValues() throws MemcachedOperationException, LockTimeoutException {
        ft.add("key", new StringHash("hash-a"), "value-a", 10);
        ft.add("key", new StringHash("hash-b"), "value-b", 10);
        ft.add("key", new StringHash("hash-c"), "value-c", 10);
        
        {
            List<String> results = ft.getValues("key", new StringHash("hash-a"), 0, 3);
            System.out.println(results);
            Assert.assertEquals(results.size(), 1);
            Assert.assertEquals(results.get(0), "value-a");
        }
        {
            List<String> results = ft.getValues("key", new StringHash("hash-b"), 0, 1);
            System.out.println(results);
            Assert.assertEquals(results.size(), 1);
            Assert.assertEquals(results.get(0), "value-b");
        }
        {
            List<String> results = ft.getValues("key", new StringHash("hash-c"), 1, 10);
            System.out.println(results);
            Assert.assertEquals(results.size(), 0);
        }
    }
    
    @Test
    public void getValues_exists() throws MemcachedOperationException, LockTimeoutException {
        ft.add("key", new StringHash("hash-a"), "value-a", 10);
        ft.add("key", new StringHash("hash-a"), "value-b", 10);
        ft.add("key", new StringHash("hash-a"), "value-c", 10);
        
        {
            List<String> results = ft.getValues("key", new StringHash("hash-a"), 0, 3);
            System.out.println(results);
            Assert.assertEquals(results.size(), 3);
            Assert.assertEquals(results.get(0), "value-a");
            Assert.assertEquals(results.get(1), "value-b");
            Assert.assertEquals(results.get(2), "value-c");
        }
        
        // nosuch hash
        List<String> rs = ft.getValues("key", new StringHash("hash-b"), 0, 100);
        Assert.assertEquals(rs.size(), 0);
    }
    
    @Test
    public void getValuesByResult() throws MemcachedOperationException, LockTimeoutException {
        ft.add("key", new StringHash("hash-a"), "value-a", 10);
        ft.add("key", new StringHash("hash-b"), "value-b", 10);
        ft.add("key", new StringHash("hash-c"), "value-c", 10);
        
        {
            List<TpListResult> results = ft.getValuesByResult("key", new StringHash("hash-a"), 0, 3);
            System.out.println(results);
            Assert.assertEquals(results.size(), 1);
            Assert.assertEquals(results.get(0).getKey(), "key" + TpFullText.KEY_SEPARATOR + "hash-a");
            Assert.assertEquals(results.get(0).getValue(), "value-a");
            Assert.assertEquals(results.get(0).getIndex(), 0);
        }
        {
            List<TpListResult> results = ft.getValuesByResult("key", new StringHash("hash-b"), 0, 1);
            System.out.println(results);
            Assert.assertEquals(results.size(), 1);
            Assert.assertEquals(results.get(0).getKey(), "key" + TpFullText.KEY_SEPARATOR + "hash-b");
            Assert.assertEquals(results.get(0).getValue(), "value-b");
            Assert.assertEquals(results.get(0).getIndex(), 0);
        }
        {
            List<TpListResult> results = ft.getValuesByResult("key", new StringHash("hash-c"), 1, 10);
            System.out.println(results);
            Assert.assertEquals(results.size(), 0);
        }
    }

    @Test
    public void getValuesByResult_exists() throws MemcachedOperationException, LockTimeoutException {
        ft.add("key", new StringHash("hash-a"), "value-a", 10);
        ft.add("key", new StringHash("hash-a"), "value-b", 10);
        ft.add("key", new StringHash("hash-a"), "value-c", 10);
        
        {
            List<TpListResult> results = ft.getValuesByResult("key", new StringHash("hash-a"), 0, 3);
            System.out.println(results);
            Assert.assertEquals(results.size(), 3);
            Assert.assertEquals(results.get(0).getKey(), "key" + TpFullText.KEY_SEPARATOR + "hash-a");
            Assert.assertEquals(results.get(0).getValue(), "value-a");
            Assert.assertEquals(results.get(0).getIndex(), 0);
            
            Assert.assertEquals(results.get(1).getKey(), "key" + TpFullText.KEY_SEPARATOR + "hash-a");
            Assert.assertEquals(results.get(1).getValue(), "value-b");
            Assert.assertEquals(results.get(1).getIndex(), 1);

            Assert.assertEquals(results.get(2).getKey(), "key" + TpFullText.KEY_SEPARATOR + "hash-a");
            Assert.assertEquals(results.get(2).getValue(), "value-c");
            Assert.assertEquals(results.get(2).getIndex(), 2);
        }
    }
    
    @Test
    public void hashCount() throws MemcachedOperationException, LockTimeoutException {
        ft.add("key1", new StringHash("hash-a"), "value-a", 10);
        ft.add("key1", new StringHash("hash-b"), "value-b", 10);
        ft.add("key1", new StringHash("hash-c"), "value-c", 10);
        
        Assert.assertEquals(ft.hashCount("key1"), 3);
        
        ft.add("key2", new StringHash("hash-d"), "value-a", 10);
        ft.add("key2", new StringHash("hash-d"), "value-b", 10);
        ft.add("key2", new StringHash("hash-d"), "value-c", 10);
        
        // TODO: unique count?
        Assert.assertEquals(ft.hashCount("key1"), 3);
    }
    
    @Test
    public void valueCount() throws MemcachedOperationException, LockTimeoutException {
        ft.add("key1", new StringHash("hash-a"), "value-a", 10);
        ft.add("key1", new StringHash("hash-b"), "value-b", 10);
        ft.add("key1", new StringHash("hash-c"), "value-c", 10);
        
        Assert.assertEquals(ft.valueCount("key1", new StringHash("hash-a")), 1);
        Assert.assertEquals(ft.valueCount("key1", new StringHash("hash-b")), 1);
        Assert.assertEquals(ft.valueCount("key1", new StringHash("hash-c")), 1);
        
        ft.add("key2", new StringHash("hash-d"), "value-a", 10);
        ft.add("key2", new StringHash("hash-d"), "value-b", 10);
        ft.add("key2", new StringHash("hash-d"), "value-c", 10);
        
        Assert.assertEquals(ft.valueCount("key2", new StringHash("hash-d")), 3);
    }
    
//    @Test
//    public void delete() throws MemcachedOperationException, LockTimeoutException {
//        ft.add("key", new StringHash("hash-a"), "value-a", 10);
//        ft.add("key", new StringHash("hash-b"), "value-b", 10);
//        ft.add("key", new StringHash("hash-c"), "value-c", 10);
//        
//        Assert.assertEquals(ft.hashCount("key"), 3);
//        {
//            List<Hash> hashes = ft.getHashes("key", 0, 3);
//            Assert.assertEquals(hashes.size(), 3);
//            Assert.assertEquals(hashes.get(0).hashValue(), "hash-a");
//            Assert.assertEquals(hashes.get(1).hashValue(), "hash-b");
//            Assert.assertEquals(hashes.get(2).hashValue(), "hash-c");
//        }
//
//        ft.delete("key", 0);
//        
//        Assert.assertEquals(ft.hashCount("key"), 0);
//        
//        List<Hash> hashes = ft.getHashes("key", 0, 3);
//        Assert.assertEquals(hashes.size(), 0);
//        
//        List<String> values = ft.getValues("key", new StringHash("hash-a"), 0, 1);
//        System.out.println(values);
//        Assert.assertEquals(values.size(), 0);
//    }
    
    @Test
    public void deleteByHash() throws MemcachedOperationException, LockTimeoutException {
        ft.add("key", new StringHash("hash-a"), "value-a", 10);
        ft.add("key", new StringHash("hash-b"), "value-b", 10);
        ft.add("key", new StringHash("hash-c"), "value-c", 10);
        
        Assert.assertEquals(ft.hashCount("key"), 3);
        Assert.assertEquals(ft.valueCount("key", new StringHash("hash-a")), 1);
        Assert.assertEquals(ft.valueCount("key", new StringHash("hash-b")), 1);
        Assert.assertEquals(ft.valueCount("key", new StringHash("hash-c")), 1);
        
        ft.deleteByHash("key", new StringHash("hash-a"), 0);
        
        // fix count size(use reindex)
        Assert.assertEquals(ft.hashCount("key"), 3);
        Assert.assertEquals(ft.valueCount("key", new StringHash("hash-a")), 0);
        Assert.assertEquals(ft.valueCount("key", new StringHash("hash-b")), 1);
        Assert.assertEquals(ft.valueCount("key", new StringHash("hash-c")), 1);
        
        {
            List<String> values = ft.getValues("key", new StringHash("hash-a"), 0, 1);
            Assert.assertEquals(values.size(), 0);
        }
        {
            List<String> values = ft.getValues("key", new StringHash("hash-b"), 0, 1);
            Assert.assertEquals(values.size(), 1);
        }
        {
            List<String> values = ft.getValues("key", new StringHash("hash-c"), 0, 1);
            Assert.assertEquals(values.size(), 1);
        }
    }
    
    @Test
    public void deleteByHash_exists() throws MemcachedOperationException, LockTimeoutException {
        ft.add("key", new StringHash("hash-a"), "value-a", 10);
        ft.add("key", new StringHash("hash-a"), "value-b", 10);
        ft.add("key", new StringHash("hash-a"), "value-c", 10);
        
        Assert.assertEquals(ft.hashCount("key"), 3);
        Assert.assertEquals(ft.valueCount("key", new StringHash("hash-a")), 3);
        
        Assert.assertTrue(ft.deleteByHash("key", new StringHash("hash-a"), 0));
        
        // fix count size(use reindex)
        Assert.assertEquals(ft.hashCount("key"), 3);
        Assert.assertEquals(ft.valueCount("key", new StringHash("hash-a")), 0);
        
        {
            List<String> values = ft.getValues("key", new StringHash("hash-a"), 0, 1);
            Assert.assertEquals(values.size(), 0);
        }
    }
    
    @Test
    public void deleteAtByHash() throws MemcachedOperationException, LockTimeoutException {
        ft.add("key", new StringHash("hash-a"), "value-a", 10);
        ft.add("key", new StringHash("hash-b"), "value-b", 10);
        ft.add("key", new StringHash("hash-c"), "value-c", 10);

        Assert.assertEquals(ft.hashCount("key"), 3);
        
        Assert.assertEquals(ft.valueCount("key", new StringHash("hash-a")), 1);
        Assert.assertEquals(ft.valueCount("key", new StringHash("hash-b")), 1);
        Assert.assertEquals(ft.valueCount("key", new StringHash("hash-c")), 1);
        
        Assert.assertTrue(ft.deleteAtByHash("key", new StringHash("hash-a"), 0, 0));
        
        // fix count size(use reindex)
        Assert.assertEquals(ft.hashCount("key"), 3);
        // fix count size(use reindex)
        Assert.assertEquals(ft.valueCount("key", new StringHash("hash-a")), 1);
        Assert.assertEquals(ft.valueCount("key", new StringHash("hash-b")), 1);
        Assert.assertEquals(ft.valueCount("key", new StringHash("hash-c")), 1);
        
        {
            List<String> values = ft.getValues("key", new StringHash("hash-a"), 0, 1);
            Assert.assertEquals(values.size(), 0);
        }
        {
            List<String> values = ft.getValues("key", new StringHash("hash-b"), 0, 1);
            Assert.assertEquals(values.size(), 1);
        }
        {
            List<String> values = ft.getValues("key", new StringHash("hash-c"), 0, 1);
            Assert.assertEquals(values.size(), 1);
        }
    }
    
    @Test
    public void deleteAtByHash_exists() throws MemcachedOperationException, LockTimeoutException {
        ft.add("key", new StringHash("hash-a"), "value-a", 10);
        ft.add("key", new StringHash("hash-a"), "value-b", 10);
        ft.add("key", new StringHash("hash-a"), "value-c", 10);
        ft.add("key", new StringHash("hash-a"), "value-d", 10);
        
        Assert.assertEquals(ft.hashCount("key"), 4);
        Assert.assertEquals(ft.valueCount("key", new StringHash("hash-a")), 4);
        
        Assert.assertTrue(ft.deleteAtByHash("key", new StringHash("hash-a"), 0, 0));
        Assert.assertTrue(ft.deleteAtByHash("key", new StringHash("hash-a"), 2, 0));
        
        // fix count size(use reindex)
        Assert.assertEquals(ft.hashCount("key"), 4);
        // fix count size(use reindex)
        Assert.assertEquals(ft.valueCount("key", new StringHash("hash-a")), 4);
        
        {
            List<String> values = ft.getValues("key", new StringHash("hash-a"), 0, 4);
            Assert.assertEquals(values.size(), 2);
            Assert.assertEquals(values.get(0), "value-b");
            Assert.assertEquals(values.get(1), "value-d");
        }
    }
    
    @Test
    public void reindexByHash_exists() throws MemcachedOperationException, LockTimeoutException {
        ft.add("key", new StringHash("hash-a"), "value-a", 10);
        ft.add("key", new StringHash("hash-a"), "value-b", 10);
        ft.add("key", new StringHash("hash-a"), "value-c", 10);
        ft.add("key", new StringHash("hash-a"), "value-d", 10);
        
        Assert.assertEquals(ft.hashCount("key"), 4);
        Assert.assertEquals(ft.valueCount("key", new StringHash("hash-a")), 4);
        
        Assert.assertTrue(ft.deleteAtByHash("key", new StringHash("hash-a"), 0, 0));
        Assert.assertTrue(ft.deleteAtByHash("key", new StringHash("hash-a"), 2, 0));
        
        // fix count size(use reindex)
        Assert.assertEquals(ft.hashCount("key"), 4);
        
        ft.reindexByHash("key", new StringHash("hash-a"));
        Assert.assertEquals(ft.valueCount("key", new StringHash("hash-a")), 2);
        
        {
            List<String> values = ft.getValues("key", new StringHash("hash-a"), 0, 4);
            Assert.assertEquals(values.size(), 2);
            Assert.assertEquals(values.get(0), "value-b");
            Assert.assertEquals(values.get(1), "value-d");
        }
    }
}
