package temperance.rpc.impl;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import temperance.core.Configure;
import temperance.core.ListCommand;
import temperance.core.Pooling;
import temperance.exception.LockTimeoutException;
import temperance.exception.MemcachedOperationException;
import temperance.exception.RpcException;
import temperance.rpc.RpcList;
import temperance.storage.TpList;
import temperance.storage.impl.MemcachedList;

public class RpcListImpl implements RpcList {
    
    protected static final Log logger = LogFactory.getLog(RpcListImpl.class);
    
    protected final Configure configure;
    
    protected final Pooling pooling;
    
    public RpcListImpl(Configure configure, Pooling pooling){
        this.configure = configure;
        this.pooling = pooling;
    }

    public Response.Add add(Request.Add request) throws RpcException {
        final String key = request.key;
        final String value = request.value;
        final int expire = request.expire;
        
        final MemcachedList list = new MemcachedList(pooling.getConnectionPool());
        try {
            list.add(key, value, expire);
            
            Response.Add response = Response.Add.newInstance();
            response.succeed = true;
            return response;
        } catch(MemcachedOperationException e){
            if(logger.isInfoEnabled()){
                logger.info(e.getMessage(), e);
            }
            
            Response.Add response = Response.Add.newInstance();
            response.succeed = false;
            return response;
        } catch(LockTimeoutException e){
            if(logger.isInfoEnabled()){
                logger.info(e.getMessage(), e);
            }
            
            Response.Add response = Response.Add.newInstance();
            response.succeed = false;
            return response;
        }
    }

    public Response.Count count(Request.Count request) throws RpcException {
        final String key = request.key;
        
        final TpList list = new MemcachedList(pooling.getConnectionPool());
        try {
            long count = list.count(key);
            
            Response.Count response = Response.Count.newInstance();
            response.count = count;
            return response;
        } catch(MemcachedOperationException e){
            throw new RpcException(e);
        }
    }

    public Response.Get get(Request.Get request) throws RpcException {
        final String key = request.key;
        final long offset = request.offset;
        final long limit = request.limit;
        
        final ListCommand command = new ListCommand(pooling);
        try {
            Future<List<String>> future = command.get(key, offset, limit);
            
            Response.Get response = Response.Get.newInstance();
            response.values = future.get();
            return response;
        } catch (InterruptedException e) {
            throw new RpcException(e);
        } catch (ExecutionException e) {
            throw new RpcException(e);
        }
    }

    public Response.Delete delete(Request.Delete request) throws RpcException {
        final String key = request.key;
        final int expire = request.expire;
        
        final TpList list = new MemcachedList(pooling.getConnectionPool());
        try {
            boolean success = list.delete(key, expire);
            
            Response.Delete response = Response.Delete.newInstance();
            response.succeed = success;
            return response;
        } catch(MemcachedOperationException e){
            if(logger.isInfoEnabled()){
                logger.info(e.getMessage(), e);
            }
            
            Response.Delete response = Response.Delete.newInstance();
            response.succeed = false;
            return response;
        } catch(LockTimeoutException e){
            if(logger.isInfoEnabled()){
                logger.info(e.getMessage(), e);
            }
            
            Response.Delete response = Response.Delete.newInstance();
            response.succeed = false;
            return response;
        }
    }
    
    public void deleteByValue(){
        
    }

}
