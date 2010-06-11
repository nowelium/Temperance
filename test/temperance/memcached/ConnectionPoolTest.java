package temperance.memcached;

import org.junit.Assert;
import org.junit.Test;

import temperance.core.ConnectionPool;
import temperance.core.Configure;
import temperance.exception.InitializationException;

public class ConnectionPoolTest {

    @Test(expected = NullPointerException.class)
    public void test0() {
        new ConnectionPool(null);
    }
    
    @Test
    public void test1() {
        try {
            Configure c = new Configure();
            c.setMemcached("dummy");
            new ConnectionPool(c).init();
            Assert.fail();
        } catch(InitializationException e){
            System.out.println(e.getMessage());
        }
    }
    
}
