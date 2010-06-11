package temperance.function;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.type.BehaviorType;

import org.chasen.mecab.wrapper.Tagger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import temperance.core.Configure;
import temperance.core.ConnectionPool;
import temperance.core.Pooling;
import temperance.exception.CommandExecutionException;
import temperance.ft.MecabHashing;
import temperance.hash.Hash;
import temperance.storage.MemcachedList;


public class DataFunctionTest {
    
    protected ConnectionPool pool;
    
    protected FunctionContext ctx;
    
    @Before
    public void setup() throws LibMemcachedException {
        Configure configure = new Configure();
        configure.setFullTextHashFunction(Hash.MD5);
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
        
        Pooling pooling = new Pooling(configure);
        pooling.init();
        
        ctx = new FunctionContext();
        ctx.setHashFunction(configure.getFullTextHashFunction());
        ctx.setNodeFilter(configure.getNodeFilter());
        ctx.setPooling(pooling);
        ctx.setTagger(Tagger.create("-r " + configure.getMecabrc()));
        
        MemcachedList a = new MemcachedList(pool);
        for(int i = 0; i < 10; ++i){
            // value starts: 1, ends: 10
            a.add("A", Integer.toString(i + 1), 0);
        }
        MemcachedList b = new MemcachedList(pool);
        for(int i = 3; i < 7; ++i){
            // value starts: 4, ends: 7
            b.add("B", Integer.toString(i + 1), 0);
        }
    }
    
    @After
    public void cleanup(){
        pool.get().getStorage().flush(0);
    }
    
    @Test
    public void selectIn() throws CommandExecutionException {
        DataFunction df = new DataFunction(ctx);
        List<String> result = df.createSelect().and("A", Arrays.asList("B"));
        Assert.assertEquals(result, Arrays.asList("4", "5", "6", "7"));
    }
    
    @Test
    public void selectNot() throws CommandExecutionException, LibMemcachedException {
        DataFunction df = new DataFunction(ctx);
        List<String> result = df.createSelect().not("A", Arrays.asList("B"));
        Assert.assertEquals(result, Arrays.asList("1", "2", "3", "8", "9", "10"));
    }
}
