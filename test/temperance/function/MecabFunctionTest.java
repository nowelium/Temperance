package temperance.function;

import java.util.Arrays;
import java.util.List;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.MemcachedClient;

import org.chasen.mecab.wrapper.Tagger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import temperance.exception.ExecutionException;
import temperance.ft.MecabHashing;
import temperance.ft.MecabNodeFilter;
import temperance.function.FunctionContext;
import temperance.function.MecabFunction;
import temperance.hash.Hash;
import temperance.hash.HashFunction;
import temperance.memcached.ConnectionPool;
import temperance.rpc.Context;
import temperance.storage.MemcachedFullText;


public class MecabFunctionTest {
    
    protected static HashFunction hashFunction = Hash.MD5;
    
    protected static Tagger tagger = Tagger.create("-r /opt/local/etc/mecabrc");
    
    protected static MecabNodeFilter filter = MecabHashing.Filter.Nouns;
    
    protected ConnectionPool pool;
    
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
        pool = new ConnectionPool(c);
    }
    
    public void setupData() throws LibMemcachedException {
        MemcachedFullText ft = new MemcachedFullText(pool);
        MecabHashing mecab = new MecabHashing(hashFunction, tagger, filter);
        
        {
            String value = "test-value-a";
            List<Long> hashes = mecab.parse("本日は晴天");
            for(int i = 0; i < hashes.size(); ++i){
                Long hash = hashes.get(i);
                ft.add("test-key", hash, value, 10);
            }
        }
        {
            String value = "test-value-b";
            List<Long> hashes = mecab.parse("本日は快晴");
            for(int i = 0; i < hashes.size(); ++i){
                Long hash = hashes.get(i);
                ft.add("test-key", hash, value, 10);
            }
        }
    }
    
    public void setupFunctionContext() {
        ctx.setPool(pool);
        ctx.setHashFunction(hashFunction);
        ctx.setNodeFilter(filter);
        ctx.setTagger(tagger);
    }
    
    public void cleanupData(){
        MemcachedClient client = pool.get();
        client.getStorage().flush(0);
    }

    @Test
    public void selectIn() throws ExecutionException {
        MecabFunction function = new MecabFunction(ctx);
        List<String> results = function.createSelect().and("test-key", Arrays.asList("本日"));
        System.out.println(results);
        Assert.assertEquals(results.size(), 2);
        Assert.assertEquals(results.get(0), "test-value-a");
        Assert.assertEquals(results.get(1), "test-value-b");
    }

    @Test
    public void selectIn_refine() throws ExecutionException {
        MecabFunction function = new MecabFunction(ctx);
        List<String> results = function.createSelect().and("test-key", Arrays.asList("本日 晴天"));
        Assert.assertEquals(results.size(), 1);
        Assert.assertEquals(results.get(0), "test-value-a");
    }
    
    @Test
    public void selectIn_refine_mecab_tag() throws ExecutionException {
        MecabFunction function = new MecabFunction(ctx);
        List<String> results = function.createSelect().and("test-key", Arrays.asList("本日は晴天なり"));
        Assert.assertEquals(results.size(), 1);
        Assert.assertEquals(results.get(0), "test-value-a");
    }
}
