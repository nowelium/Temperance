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

import temperance.exception.CommandExecutionException;
import temperance.ft.MecabHashing;
import temperance.hash.Hash;
import temperance.memcached.ConnectionPool;
import temperance.rpc.Context;
import temperance.storage.MemcachedList;

public class ValueFunctionTest {
    
    protected Context context;
    
    protected ConnectionPool pool;
    
    protected FunctionContext ctx;
    
    @Before
    public void setup() throws LibMemcachedException {
        context = new Context();
        context.setFullTextHashFunction(Hash.MD5);
        context.setMecabrc("/opt/local/etc/mecabrc");
        context.setMemcached("localhost:11211");
        context.setMemcachedPoolSize(10);
        context.setNodeFilter(MecabHashing.Filter.Nouns);
        context.setPoolBehaviors(new HashMap<BehaviorType, Boolean>() {
            private static final long serialVersionUID = 1L;
            {
                put(BehaviorType.SUPPORT_CAS, Boolean.TRUE);
                put(BehaviorType.TCP_KEEPALIVE, Boolean.TRUE);
                put(BehaviorType.TCP_NODELAY, Boolean.TRUE);
                put(BehaviorType.BUFFER_REQUESTS, Boolean.FALSE);
            }
        });
        
        pool = new ConnectionPool(context);
        pool.init();
        
        ctx = new FunctionContext();
        ctx.setHashFunction(context.getFullTextHashFunction());
        ctx.setNodeFilter(context.getNodeFilter());
        ctx.setPool(pool);
        ctx.setTagger(Tagger.create("-r " + context.getMecabrc()));
        
        MemcachedList list = new MemcachedList(pool);
        for(int i = 0; i < 10; ++i){
            // value starts: 1, ends: 10
            list.add("hoge:0", Integer.toString(i + 1), 0);
        }
    }
    
    @After
    public void cleanup(){
        pool.get().getStorage().flush(0);
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
