package temperance.storage.impl;

import junit.framework.Assert;
import java.util.HashMap;
import libmemcached.wrapper.type.BehaviorType;
import temperance.core.Configure;
import temperance.core.ConnectionPool;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import temperance.exception.MemcachedOperationException;

/**
 *
 * @author nowel
 */
public class MemcachedQueueTest {

    private static ConnectionPool pool;
    
    private MemcachedQueue queue;
    
    @BeforeClass
    public static void setupPool(){
        Configure configure = new Configure();
        configure.setMemcached("localhost:11211");
        configure.setPoolBehaviors(new HashMap<BehaviorType, Boolean>(){
            private static final long serialVersionUID = 1L;
            {
                put(BehaviorType.BUFFER_REQUESTS, Boolean.FALSE);
            }
        });
        pool = new ConnectionPool(configure);
        pool.init();
    }
    
    @AfterClass
    public static void cleanupPool(){
        //pool.destroy();
    }
    
    @Before
    public void setup(){
        queue = new MemcachedQueue(pool);
    }
    
    @After
    public void cleanup(){
        pool.get().getStorage().flush(0);
    }
    

    /**
     * Test of enqueue method, of class MemcachedQueue.
     */
    @Test
    public void testEnqueue() throws MemcachedOperationException {
        Assert.assertTrue(queue.enqueue("key", "hoge", 0));
        Assert.assertTrue(queue.enqueue("key", "foo", 0));
        Assert.assertTrue(queue.enqueue("key", "bar", 0));
        
        Assert.assertEquals(queue.dequeue("key"), "hoge");
        Assert.assertEquals(queue.dequeue("key"), "foo");
        Assert.assertEquals(queue.dequeue("key"), "bar");
        
        Assert.assertNull(queue.dequeue("key"));
        Assert.assertNull(queue.dequeue("key"));
        Assert.assertNull(queue.dequeue("key"));
        
        Assert.assertTrue(queue.enqueue("key", "baz", 0));
        Assert.assertEquals(queue.dequeue("key"), "baz");
    }

}