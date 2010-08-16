package temperance.rpc.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import temperance.core.Configure;
import temperance.core.Pooling;
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
        
        final ExecutorService exec = Executors.newSingleThreadExecutor();
        final Response.Dequeue response = Response.Dequeue.newInstance();
        try {
            Future<String> future = exec.submit(new Dequeuer(key));
            response.value = future.get(timeout, unit);
        } catch(TimeoutException e){
            response.value = null;
        } catch (InterruptedException e) {
            if(logger.isErrorEnabled()){
                logger.error(e.getMessage(), e);
            }
            throw new RpcException(e);
        } catch (ExecutionException e) {
            if(logger.isErrorEnabled()){
                logger.error(e.getMessage(), e);
            }
            throw new RpcException(e);
        } finally {
            exec.shutdownNow();
        }
        return response;
    }
    
    protected class Dequeuer implements Callable<String> {
        protected final String key;
        protected final TpQueue queue = new MemcachedQueue(pooling.getConnectionPool());
        protected Dequeuer(String key){
            this.key = key;
        }
        public String call() throws Exception {
            while(true){
                String value = queue.dequeue(key);
                if(null != value){
                    return value;
                }
                
                TimeUnit.MILLISECONDS.sleep(100);
            }
        }
    }

}
