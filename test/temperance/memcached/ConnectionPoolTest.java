package temperance.memcached;

import org.junit.Assert;
import org.junit.Test;

import temperance.exception.InitializationException;
import temperance.rpc.Context;

public class ConnectionPoolTest {

    @Test(expected = NullPointerException.class)
    public void test0() {
        new ConnectionPool(null);
    }
    
    @Test
    public void test1() {
        try {
            Context ctx = new Context();
            ctx.setMemcached("dummy");
            new ConnectionPool(ctx).init();
            Assert.fail();
        } catch(InitializationException e){
            System.out.println(e.getMessage());
        }
    }
    
}
