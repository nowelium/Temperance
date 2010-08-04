package temperance.storage.impl;

import java.util.List;
import temperance.storage.TpMap.TpMapResult;
import java.util.Arrays;
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
import temperance.storage.TpMap;

/**
 *
 * @author nowel
 */
public class MemcachedMapTest {

    private static ConnectionPool pool;
    
    private MemcachedMap map;
    
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
        map = new MemcachedMap(pool);
    }
    
    @After
    public void cleanup(){
        pool.get().getStorage().flush(0);
    }
    

    /**
     * Test of set method, of class MemcachedMap.
     */
    @Test
    public void testSet() {
        Assert.assertTrue(map.set("key-1", "value-1", 10));
        Assert.assertTrue(map.set("key-1", "value-2", 10));
        Assert.assertTrue(map.set("key-2", "value-1", 10));
    }

    /**
     * Test of get method, of class MemcachedMap.
     */
    @Test
    public void testGet() throws Exception {
        map.set("key-1", "value-1", 10);
        
        Assert.assertEquals(map.get("key-1"), "value-1");
        
        map.set("key-1", "value-2", 10);
        
        Assert.assertEquals(map.get("key-1"), "value-2");
        
        map.set("key-2", "value-1", 10);
        
        Assert.assertEquals(map.get("key-2"), "value-1");
    }

    /**
     * Test of delete method, of class MemcachedMap.
     */
    @Test
    public void testDelete() throws Exception {
        map.set("key-1", "value-1", 10);
        
        Assert.assertEquals(map.get("key-1"), "value-1");
        
        Assert.assertTrue(map.delete("key-1", 0));
        
        Assert.assertNull(map.get("key-1"));
        
        Assert.assertFalse(map.delete("no-set-key", 0));
    }

    
    /**
     * Test of getValuesByResult method, of class MemcachedMap.
     */
    @Test
    public void testGetValuesByResult() throws Exception {
        map.set("key-1", "value-1", 10);
        map.set("key-2", "value-2", 10);
        map.set("key-3", "value-3", 10);
        
        List<TpMapResult> results = map.getValuesByResult(Arrays.asList("key-1", "key-2", "key-3"));
        {
            TpMapResult result = results.get(0);
            Assert.assertEquals(result.getKey(), MemcachedMap.DEFAULT_ROOT_KEY_PREFIX + TpMap.KEY_SEPARATOR + "key-1");
            Assert.assertEquals(result.getValue(), "value-1");
        }
        {
            TpMapResult result = results.get(1);
            Assert.assertEquals(result.getKey(), MemcachedMap.DEFAULT_ROOT_KEY_PREFIX + TpMap.KEY_SEPARATOR + "key-2");
            Assert.assertEquals(result.getValue(), "value-2");
        }
        {
            TpMapResult result = results.get(2);
            Assert.assertEquals(result.getKey(), MemcachedMap.DEFAULT_ROOT_KEY_PREFIX + TpMap.KEY_SEPARATOR + "key-3");
            Assert.assertEquals(result.getValue(), "value-3");
        }
    }

}