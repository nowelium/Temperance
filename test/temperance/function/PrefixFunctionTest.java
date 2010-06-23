package temperance.function;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.type.BehaviorType;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import temperance.core.Configure;
import temperance.core.Pooling;
import temperance.exception.CommandExecutionException;
import temperance.hash.Hash;
import temperance.hash.HashFunction;
import temperance.hashing.PrefixHashing;
import temperance.storage.impl.MemcachedFullText;

public class PrefixFunctionTest {
    
    protected static HashFunction hashFunction = Hash.MD5;
    
    protected Pooling pooling;
    
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
        Configure c = new Configure();
        c.setMaxConnectionPoolSize(1);
        c.setMemcached("localhost:11211");
        c.setPoolBehaviors(new HashMap<BehaviorType, Boolean>() {
            private static final long serialVersionUID = 1L;
            {
                put(BehaviorType.SUPPORT_CAS, Boolean.TRUE);
                put(BehaviorType.TCP_KEEPALIVE, Boolean.TRUE);
                put(BehaviorType.TCP_NODELAY, Boolean.TRUE);
                put(BehaviorType.BUFFER_REQUESTS, Boolean.FALSE);
            }
        });
        
        pooling = new Pooling(c);
        pooling.init();
    }
    
    public void setupData() throws LibMemcachedException {
        MemcachedFullText ft = new MemcachedFullText(pooling.getConnectionPool());
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
        ctx.setPooling(pooling);
        ctx.setHashFunction(hashFunction);
    }
    
    public void cleanupData(){
        MemcachedClient client = pooling.getConnectionPool().get();
        client.getStorage().flush(0);
    }

    @Test
    public void selectIn() throws CommandExecutionException {
        PrefixFunction function = new PrefixFunction(ctx);
        List<String> results = function.createSelect().and("test-key", Arrays.asList("本日"));
        System.out.println(results);
        Assert.assertEquals(results.size(), 2);
        Assert.assertEquals(results.get(0), "test-value-a");
        Assert.assertEquals(results.get(1), "test-value-b");
    }
    
    @Test
    public void selectIn_prefix() throws CommandExecutionException {
        PrefixFunction function = new PrefixFunction(ctx);
        List<String> results = function.createSelect().and("test-key", Arrays.asList("本日は晴"));
        System.out.println(results);
        Assert.assertEquals(results.size(), 1);
        Assert.assertEquals(results.get(0), "test-value-a");
    }
}
