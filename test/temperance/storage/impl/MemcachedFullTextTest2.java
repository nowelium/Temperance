package temperance.storage.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import libmemcached.wrapper.type.BehaviorType;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import temperance.core.Configure;
import temperance.core.ConnectionPool;
import temperance.exception.LockTimeoutException;
import temperance.exception.MemcachedOperationException;
import temperance.hash.Hash;
import temperance.hash.StringHash;
import temperance.storage.TpList.TpListResult;


public class MemcachedFullTextTest2 {

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
    @Ignore
    public void multiHashSingleValue() throws MemcachedOperationException, LockTimeoutException {
        List<StringHash> hashes = Arrays.asList(new StringHash("hash-1"), new StringHash("hash-2"), new StringHash("hash-3"));
        for(StringHash h: hashes){
            ft.add("key", h, "value", 10);
        }
        
        List<String> values = ft.getValues("key", new StringHash("hash-1"), 0, 10);
        Assert.assertEquals(values.size(), 1);
        Assert.assertEquals(values.get(0), "value");
        
        List<Hash> valueHashes = ft.getHashesByValue("key", "value", 0, 10);
        Assert.assertEquals(valueHashes.size(), 3);
        Assert.assertEquals(valueHashes.get(0), new StringHash("hash-1"));
        Assert.assertEquals(valueHashes.get(1), new StringHash("hash-2"));
        Assert.assertEquals(valueHashes.get(2), new StringHash("hash-3"));
        
        ft.deleteByHash("key", new StringHash("hash-1"), 0);
        Assert.assertEquals(ft.getValues("key", new StringHash("hash-1"), 0, 10).size(), 0);
    }
    
    @Test
    @Ignore
    public void multiHashMultValue() throws MemcachedOperationException, LockTimeoutException {
        {
            List<StringHash> hashes = Arrays.asList(new StringHash("hash-1"), new StringHash("hash-2"), new StringHash("hash-3"));
            for(StringHash h: hashes){
                ft.add("key", h, "value-1", 10);
            }
        }
        {
            List<StringHash> hashes = Arrays.asList(new StringHash("hash-4"), new StringHash("hash-5"), new StringHash("hash-6"));
            for(StringHash h: hashes){
                ft.add("key", h, "value-1", 10);
            }
        }
        
        List<String> values_1 = ft.getValues("key", new StringHash("hash-1"), 0, 10);
        Assert.assertEquals(values_1.size(), 1);
        Assert.assertEquals(values_1.get(0), "value-1");
        
        // before check
        {
            List<Hash> beforeValueHashes = ft.getHashesByValue("key", "value-1", 0, 10);
            Assert.assertEquals(beforeValueHashes.size(), 6);
            Assert.assertEquals(beforeValueHashes.get(0), new StringHash("hash-1"));
            Assert.assertEquals(beforeValueHashes.get(1), new StringHash("hash-2"));
            Assert.assertEquals(beforeValueHashes.get(2), new StringHash("hash-3"));
            Assert.assertEquals(beforeValueHashes.get(3), new StringHash("hash-4"));
            Assert.assertEquals(beforeValueHashes.get(4), new StringHash("hash-5"));
            Assert.assertEquals(beforeValueHashes.get(5), new StringHash("hash-6"));
            
            List<Hash> hashes = ft.getHashes("key", 0, 10);
            Assert.assertEquals(hashes.size(), 6);
            Assert.assertEquals(hashes.get(0), new StringHash("hash-1"));
            Assert.assertEquals(hashes.get(1), new StringHash("hash-2"));
            Assert.assertEquals(hashes.get(2), new StringHash("hash-3"));
            Assert.assertEquals(hashes.get(3), new StringHash("hash-4"));
            Assert.assertEquals(hashes.get(4), new StringHash("hash-5"));
            Assert.assertEquals(hashes.get(5), new StringHash("hash-6"));
        }
        
        ft.deleteByHash("key", new StringHash("hash-3"), 0);
        
        {
            List<Hash> afterValueHashes = ft.getHashesByValue("key", "value-1", 0, 10);
            System.out.println(afterValueHashes);
            //
            // TODO: deleteByHash does not delte 
            //
            Assert.assertEquals(afterValueHashes.size(), 6);
            Assert.assertEquals(afterValueHashes.get(0), new StringHash("hash-1"));
            Assert.assertEquals(afterValueHashes.get(1), new StringHash("hash-2"));
            Assert.assertEquals(afterValueHashes.get(2), new StringHash("hash-3"));
            Assert.assertEquals(afterValueHashes.get(3), new StringHash("hash-4"));
            Assert.assertEquals(afterValueHashes.get(4), new StringHash("hash-5"));
            Assert.assertEquals(afterValueHashes.get(5), new StringHash("hash-6"));
            
            Assert.assertEquals(ft.getValuesByResult("key", new StringHash("hash-1"), 0, 10).size(), 1);
            Assert.assertEquals(ft.getValuesByResult("key", new StringHash("hash-2"), 0, 10).size(), 1);
            Assert.assertEquals(ft.getValuesByResult("key", new StringHash("hash-3"), 0, 10).size(), 0);
            Assert.assertEquals(ft.getValuesByResult("key", new StringHash("hash-4"), 0, 10).size(), 1);
            Assert.assertEquals(ft.getValuesByResult("key", new StringHash("hash-5"), 0, 10).size(), 1);
            Assert.assertEquals(ft.getValuesByResult("key", new StringHash("hash-6"), 0, 10).size(), 1);
        }
    }
    
    @Test
    public void deleteValue() throws MemcachedOperationException, LockTimeoutException {
        ft.add("key", new StringHash("hash-1"), "value-1", 0);
        ft.add("key", new StringHash("hash-1"), "value-2", 0);
        ft.add("key", new StringHash("hash-1"), "value-3", 0);
        
        {
            List<String> values = ft.getValues("key", new StringHash("hash-1"), 0, 10);
            Assert.assertEquals(values.size(), 3);
            Assert.assertEquals(values.get(0), "value-1");
            Assert.assertEquals(values.get(1), "value-2");
            Assert.assertEquals(values.get(2), "value-3");
        }
        
        String deleteValue = "value-2";
        
        final long hashCount = ft.hashCountByValue("key", deleteValue);
        System.out.println(hashCount);
        for(long i = 0; i < hashCount; i += 1000){
            List<Hash> hashes = ft.getHashesByValue("key", deleteValue, i, 1000);
            for(Hash hash: hashes){
                final long valueCount = ft.valueCount("key", hash);
                for(long j = 0; j < valueCount; j += 1000){
                    List<TpListResult> results = ft.getValuesByResult("key", hash, j, 1000);
                    for(TpListResult result: results){
                        if(deleteValue.equals(result.getValue())){
                            ft.deleteAtByHash("key", hash, result.getIndex(), 0);
                        }
                    }
                }
            }
        }
        
        {
            List<String> values = ft.getValues("key", new StringHash("hash-1"), 0, 10);
            Assert.assertEquals(values.size(), 2);
            Assert.assertEquals(values.get(0), "value-1");
            Assert.assertEquals(values.get(1), "value-3");
        }
    }
}
