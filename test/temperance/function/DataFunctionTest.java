package temperance.function;

import java.util.Arrays;
import java.util.List;

import libmemcached.exception.LibMemcachedException;

import org.chasen.mecab.wrapper.Tagger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import temperance.exception.ExecutionException;
import temperance.ft.MecabHashing;
import temperance.hash.Hash;
import temperance.memcached.ConnectionPool;
import temperance.rpc.Context;
import temperance.storage.MemcachedList;


public class DataFunctionTest {
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
        
        pool = new ConnectionPool(context);
        pool.init();
        
        ctx = new FunctionContext();
        ctx.setHashFunction(context.getFullTextHashFunction());
        ctx.setNodeFilter(context.getNodeFilter());
        ctx.setPool(pool);
        ctx.setTagger(Tagger.create("-r " + context.getMecabrc()));
        
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
    public void selectIn() throws ExecutionException {
        DataFunction df = new DataFunction(ctx);
        List<String> result = df.createSelect().and("A", Arrays.asList("B"));
        Assert.assertEquals(result, Arrays.asList("4", "5", "6", "7"));
    }
    
    @Test
    public void selectNot() throws ExecutionException, LibMemcachedException {
        DataFunction df = new DataFunction(ctx);
        List<String> result = df.createSelect().not("A", Arrays.asList("B"));
        Assert.assertEquals(result, Arrays.asList("1", "2", "3", "8", "9", "10"));
    }
}
