package temperance.handler.function;

import java.util.Arrays;
import java.util.List;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.MemcachedClient;

import org.chasen.mecab.wrapper.Tagger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import temperance.ft.MecabHashing;
import temperance.ft.MecabNodeFilter;
import temperance.handler.function.exception.ExecutionException;
import temperance.hash.Hash;
import temperance.hash.HashFunction;
import temperance.storage.MemcachedFullText;


public class MecabFunctionTest {
    
    protected static HashFunction hashFunction = Hash.MD5;
    
    protected static Tagger tagger = Tagger.create("-r /opt/local/etc/mecabrc");
    
    protected static MecabNodeFilter filter = MecabHashing.Filter.Nouns;
    
    protected MemcachedClient client = new MemcachedClient();
    
    protected FunctionContext ctx = new FunctionContext();
    
    @Before
    public void before() throws LibMemcachedException {
        connectMemcachedClient();
        setupData();
        setupFunctionContext();
    }
    
    @After
    public void after(){
        cleanupData();
    }
    
    public void connectMemcachedClient(){
        client.getServerList().parse("localhost:11211").push();
    }
    
    public void setupData() throws LibMemcachedException {
        MemcachedFullText ft = new MemcachedFullText(client);
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
        ctx.setClient(client);
        ctx.setHashFunction(hashFunction);
        ctx.setNodeFilter(filter);
        ctx.setTagger(tagger);
    }
    
    public void cleanupData(){
        client.getServerList().parse("localhost:11211").push();
        client.getStorage().flush(0);
    }

    @Test
    @Ignore
    public void selectId() throws ExecutionException {
        MecabFunction function = new MecabFunction(ctx);
        List<String> results = function.selectIn("test-key", Arrays.asList("本日"));
        System.out.println(results);
        Assert.assertEquals(results.size(), 2);
        Assert.assertEquals(results.get(0), "test-value-a");
        Assert.assertEquals(results.get(1), "test-value-b");
    }

    @Test
    public void selectId_refine() throws ExecutionException {
        MecabFunction function = new MecabFunction(ctx);
        List<String> results = function.selectIn("test-key", Arrays.asList("本日 晴天"));
        Assert.assertEquals(results.size(), 1);
        Assert.assertEquals(results.get(0), "test-value-a");
    }
    
    @Test
    public void selectId_refine_mecab_tag() throws ExecutionException {
        MecabFunction function = new MecabFunction(ctx);
        List<String> results = function.selectIn("test-key", Arrays.asList("本日は晴天なり"));
        Assert.assertEquals(results.size(), 1);
        Assert.assertEquals(results.get(0), "test-value-a");
    }
}
