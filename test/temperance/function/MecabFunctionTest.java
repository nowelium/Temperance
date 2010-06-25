package temperance.function;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.type.BehaviorType;

import org.chasen.mecab.wrapper.Tagger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import temperance.core.Configure;
import temperance.core.Pooling;
import temperance.exception.CommandExecutionException;
import temperance.exception.LockTimeoutException;
import temperance.exception.MemcachedOperationException;
import temperance.hash.Digest;
import temperance.hash.Hash;
import temperance.hash.HashFunction;
import temperance.hashing.MecabHashing;
import temperance.hashing.MecabNodeFilter;
import temperance.storage.impl.MemcachedFullText;


public class MecabFunctionTest {
    
    protected static HashFunction hashFunction = Digest.MD5;
    
    protected static Tagger tagger = Tagger.create("-r /opt/local/etc/mecabrc");
    
    protected static MecabNodeFilter filter = MecabHashing.Filter.Nouns;
    
    protected Pooling pooling;
    
    protected FunctionContext ctx = new FunctionContext();
    
    @Before
    public void before() throws MemcachedOperationException, LockTimeoutException {
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
        c.setMaxConnectionPoolSize(10);
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
    
    public void setupData() throws MemcachedOperationException, LockTimeoutException {
        MemcachedFullText ft = new MemcachedFullText(pooling.getConnectionPool());
        MecabHashing mecab = new MecabHashing(hashFunction, tagger, filter);
        
        {
            String value = "test-value-a";
            List<Hash> hashes = mecab.parse("本日は晴天");
            for(int i = 0; i < hashes.size(); ++i){
                Hash hash = hashes.get(i);
                ft.add("test-key", hash, value, 600);
            }
        }
        {
            String value = "test-value-b";
            List<Hash> hashes = mecab.parse("本日は快晴");
            for(int i = 0; i < hashes.size(); ++i){
                Hash hash = hashes.get(i);
                ft.add("test-key", hash, value, 600);
            }
        }
    }
    
    public void setupFunctionContext() {
        ctx.setPooling(pooling);
        ctx.setHashFunction(hashFunction);
        ctx.setNodeFilter(filter);
        ctx.setTagger(tagger);
    }
    
    public void cleanupData(){
        MemcachedClient client = pooling.getConnectionPool().get();
        client.getStorage().flush(0);
    }

    @Test
    public void selectIn() throws CommandExecutionException {
        MecabFunction function = new MecabFunction(ctx);
        {
            List<String> results = function.createSelect().and("test-key", Arrays.asList("本日"));
            System.out.println(results);
            Assert.assertEquals(results.size(), 2);
            Assert.assertEquals(results.get(0), "test-value-a");
            Assert.assertEquals(results.get(1), "test-value-b");
        }
        {
            List<String> results = function.createSelect().and("test-key", Arrays.asList("晴天"));
            System.out.println(results);
            Assert.assertEquals(results.size(), 1);
            Assert.assertEquals(results.get(0), "test-value-a");
        }
    }

    @Test
    public void selectIn_refine() throws CommandExecutionException {
        MecabFunction function = new MecabFunction(ctx);
        List<String> results = function.createSelect().and("test-key", Arrays.asList("本日 晴天"));
        Assert.assertEquals(results.size(), 1);
        Assert.assertEquals(results.get(0), "test-value-a");
    }
    
    @Test
    public void selectIn_refine_mecab_tag() throws CommandExecutionException {
        MecabFunction function = new MecabFunction(ctx);
        List<String> results = function.createSelect().and("test-key", Arrays.asList("本日は晴天なり"));
        Assert.assertEquals(results.size(), 1);
        Assert.assertEquals(results.get(0), "test-value-a");
    }
}
