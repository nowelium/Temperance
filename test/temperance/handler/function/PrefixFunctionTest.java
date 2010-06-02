package temperance.handler.function;

import java.util.Arrays;
import java.util.List;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.MemcachedClient;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import temperance.ft.PrefixHashing;
import temperance.handler.Context;
import temperance.handler.function.exception.ExecutionException;
import temperance.hash.Hash;
import temperance.hash.HashFunction;
import temperance.memcached.Pool;
import temperance.storage.MemcachedFullText;

public class PrefixFunctionTest {
    
    protected static HashFunction hashFunction = Hash.MD5;
    
    protected Pool pool;
    
    protected FunctionContext ctx = new FunctionContext();
    
    @Before
    public void before() throws LibMemcachedException {
        setupPool();
        setupData();
        setupFunctionContext();
    }
    
    @After
    public void after(){
        cleanupData();
    }

    public void setupPool(){
        Context c = new Context();
        c.setMemcachedPoolSize(1);
        c.setMemcached("localhost:11211");
        pool = new Pool(c);
    }
    
    public void setupData() throws LibMemcachedException {
        MemcachedFullText ft = new MemcachedFullText(pool);
        PrefixHashing hashing = new PrefixHashing(hashFunction);
        
        {
            String value = "test-value-a";
            List<Long> hashes = hashing.parse("本日は晴天");
            for(int i = 0; i < hashes.size(); ++i){
                Long hash = hashes.get(i);
                ft.add("test-key", hash, value, 10);
            }
        }
        {
            String value = "test-value-b";
            List<Long> hashes = hashing.parse("本日は快晴");
            for(int i = 0; i < hashes.size(); ++i){
                Long hash = hashes.get(i);
                ft.add("test-key", hash, value, 10);
            }
        }
    }
    
    public void setupFunctionContext() {
        ctx.setPool(pool);
        ctx.setHashFunction(hashFunction);
    }
    
    public void cleanupData(){
        MemcachedClient client = pool.get();
        client.getStorage().flush(0);
    }

    @Test
    public void selectIn() throws ExecutionException {
        PrefixFunction function = new PrefixFunction(ctx);
        List<String> results = function.selectIn("test-key", Arrays.asList("本日"));
        System.out.println(results);
        Assert.assertEquals(results.size(), 2);
        Assert.assertEquals(results.get(0), "test-value-a");
        Assert.assertEquals(results.get(1), "test-value-b");
    }
    
    @Test
    public void selectIn_prefix() throws ExecutionException {
        PrefixFunction function = new PrefixFunction(ctx);
        List<String> results = function.selectIn("test-key", Arrays.asList("本日は晴"));
        System.out.println(results);
        Assert.assertEquals(results.size(), 1);
        Assert.assertEquals(results.get(0), "test-value-a");
    }
}
