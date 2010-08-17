package temperance.rpc.impl;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import libmemcached.wrapper.type.BehaviorType;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import temperance.core.Configure;
import temperance.core.Pooling;
import temperance.exception.RpcException;
import temperance.hash.Digest;
import temperance.hashing.MecabHashing;
import temperance.rpc.RpcQueue.Request;
import temperance.rpc.RpcQueue.Response;

public class RpcQueueImplTest {
    
    private Configure configure;
    
    private Pooling pooling;
    
    @Before
    public void before(){
        configure = new Configure();
        configure.setFullTextHashFunction(Digest.MD5);
        configure.setMemcached("localhost:11211");
        configure.setNodeFilter(MecabHashing.Filter.Nouns);
        configure.setPoolBehaviors(new HashMap<BehaviorType, Boolean>(){
            private static final long serialVersionUID = 1L;
            {
                put(BehaviorType.BUFFER_REQUESTS, Boolean.FALSE);
            }
        });
        
        pooling = new Pooling(configure);
        pooling.init();
    }
    
    @After
    public void after(){
        pooling.getConnectionPool().get().getStorage().flush(0);
        pooling.destroy();
    }
    
    @Test
    public void testDequeue() throws RpcException {
        RpcQueueImpl queue = new RpcQueueImpl(configure, pooling);
        // enqueue
        {
            Request.Enqueue request = Request.Enqueue.newInstance();
            request.key = "key";
            request.value = "value-1";
            Assert.assertTrue(queue.enqueue(request).succeed);
        }
        {
            Request.Enqueue request = Request.Enqueue.newInstance();
            request.key = "key";
            request.value = "value-2";
            Assert.assertTrue(queue.enqueue(request).succeed);
        }
        
        // dequeue
        {
            Request.Dequeue request = Request.Dequeue.newInstance();
            request.key = "key";
            request.timeout = 5;
            Response.Dequeue response = queue.dequeue(request);
            System.out.println(response.value);
            Assert.assertEquals(response.value, "value-1");
        }
        {
            Request.Dequeue request = Request.Dequeue.newInstance();
            request.key = "key";
            request.timeout = 5;
            Response.Dequeue response = queue.dequeue(request);
            System.out.println(response.value);
            Assert.assertEquals(response.value, "value-2");
        }
        // wait 5
        {
            long start = System.currentTimeMillis();
            
            Request.Dequeue request = Request.Dequeue.newInstance();
            request.key = "key";
            request.timeout = 5;
            Response.Dequeue response = queue.dequeue(request);
            long end = System.currentTimeMillis();
            
            System.out.println(response.value);
            Assert.assertNull(response.value);
            Assert.assertTrue(TimeUnit.SECONDS.toMillis(5) <= (end - start));
        }
    }

}
