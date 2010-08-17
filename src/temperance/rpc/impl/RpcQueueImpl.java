package temperance.rpc.impl;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import temperance.core.Configure;
import temperance.core.Pooling;
import temperance.exception.MemcachedOperationException;
import temperance.exception.RpcException;
import temperance.rpc.RpcQueue;
import temperance.storage.TpQueue;
import temperance.storage.impl.MemcachedQueue;

public class RpcQueueImpl implements RpcQueue {
    
    protected static final Log logger = LogFactory.getLog(RpcQueueImpl.class);
    
    protected final Configure configure;
    
    protected final Pooling pooling;
    
    public RpcQueueImpl(Configure configure, Pooling pooling){
        this.configure = configure;
        this.pooling = pooling;
    }

    public Response.Enqueue enqueue(Request.Enqueue request) throws RpcException {
        final String key = request.key;
        final String value = request.value;
        final int expire = request.expire;
        
        final TpQueue queue = new MemcachedQueue(pooling.getConnectionPool());
        final Response.Enqueue response = Response.Enqueue.newInstance();
        response.succeed = queue.enqueue(key, value, expire);
        return response;
    }
    
    public Response.Dequeue dequeue(Request.Dequeue request) throws RpcException {
        final String key = request.key;
        final int timeout = request.timeout;
        final TimeUnit unit = request.unit;
        
        final TpQueue queue = new MemcachedQueue(pooling.getConnectionPool());
        final long executionTimeout = System.currentTimeMillis() + unit.toMillis(timeout);
        try {
            while(true){
                long current = System.currentTimeMillis();
                if(executionTimeout < current){
                    Response.Dequeue response = Response.Dequeue.newInstance();
                    response.value = null;
                    return response;
                }
                
                String result = queue.dequeue(key);
                if(null != result){
                    Response.Dequeue response = Response.Dequeue.newInstance();
                    response.value = result;
                    return response;
                }
                
                TimeUnit.MILLISECONDS.sleep(100);
            }
        } catch(MemcachedOperationException e){
            if(logger.isErrorEnabled()){
                logger.error(e.getMessage(), e);
            }
            
            throw new RpcException(e);
        } catch(InterruptedException e){
            throw new RpcException(e);
        }
    }
    
}
