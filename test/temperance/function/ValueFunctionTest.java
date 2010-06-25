package temperance.function;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
import temperance.hashing.MecabHashing;
import temperance.storage.impl.MemcachedList;

public class ValueFunctionTest {
    
    protected Pooling pooling;
    
    protected FunctionContext ctx;
    
    @Before
    public void setup() throws MemcachedOperationException, LockTimeoutException {
        Configure configure = new Configure();
        configure.setFullTextHashFunction(Digest.MD5);
        configure.setMecabrc("/opt/local/etc/mecabrc");
        configure.setMemcached("localhost:11211");
        configure.setMaxConnectionPoolSize(10);
        configure.setNodeFilter(MecabHashing.Filter.Nouns);
        configure.setPoolBehaviors(new HashMap<BehaviorType, Boolean>() {
            private static final long serialVersionUID = 1L;
            {
                put(BehaviorType.SUPPORT_CAS, Boolean.TRUE);
                put(BehaviorType.TCP_KEEPALIVE, Boolean.TRUE);
                put(BehaviorType.TCP_NODELAY, Boolean.TRUE);
                put(BehaviorType.BUFFER_REQUESTS, Boolean.FALSE);
            }
        });
        
        pooling = new Pooling(configure);
        pooling.init();
        
        ctx = new FunctionContext();
        ctx.setHashFunction(configure.getFullTextHashFunction());
        ctx.setNodeFilter(configure.getNodeFilter());
        ctx.setPooling(pooling);
        ctx.setTagger(Tagger.create("-r " + configure.getMecabrc()));
        
        MemcachedList list = new MemcachedList(pooling.getConnectionPool());
        for(int i = 0; i < 10; ++i){
            // value starts: 1, ends: 10
            list.add("hoge:0", Integer.toString(i + 1), 0);
        }
    }
    
    @After
    public void cleanup(){
        pooling.getConnectionPool().get().getStorage().flush(0);
    }
    
    @Test
    public void selectIn() throws CommandExecutionException {
        ValueFunction function = new ValueFunction(ctx);
        List<String> results = function.createSelect().and("hoge:0", Arrays.asList("1", "3", "9", "10", "11", "20"));
        Assert.assertEquals(results, Arrays.asList("1", "3", "9", "10"));
        System.out.println(results);
    }
    
    @Test
    public void selectNot() throws CommandExecutionException {
        ValueFunction function = new ValueFunction(ctx);
        List<String> results = function.createSelect().not("hoge:0", Arrays.asList("1", "3", "9", "10", "11", "20"));
        Assert.assertEquals(results, Arrays.asList("2", "4", "5", "6", "7", "8"));
        System.out.println(results);
    }
    
    @Test
    public void selectOr() throws CommandExecutionException {
        ValueFunction function = new ValueFunction(ctx);
        List<String> results = function.createSelect().or("hoge:0", Arrays.asList("1", "3", "9", "10", "11", "20"));
        Assert.assertEquals(results, Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));
        System.out.println(results);
    }

}
