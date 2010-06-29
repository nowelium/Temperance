package temperance.rpc.impl;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import temperance.core.Configure;
import temperance.core.ListCommand;
import temperance.core.Pooling;
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
        final boolean async = request.asyncRequest;
        
        if(logger.isDebugEnabled()){
            logger.debug(new StringBuilder("add (")
                .append("key=").append(key).append(",")
                .append("value=").append(value).append(",")
                .append("expire=").append(expire).append(",")
                .append("async=").append(async)
                .append(")")
            );
        }
        
        final ListCommand command = new ListCommand(pooling);
        final Future<Long> future = command.add(key, value, expire);
        if(async){
            Response.Add response = Response.Add.newInstance();
            response.status = Response.Status.ENQUEUE;
            return response;
        }
        
        //
        // sync request
        //
        Response.Add response = Response.Add.newInstance();
        try {
            future.get(); // TODO: id value
            
            response.status = Response.Status.SUCCESS;
        } catch(InterruptedException e){
            if(logger.isInfoEnabled()){
                logger.info(e.getMessage(), e);
            }
            
            response.status = Response.Status.FAILURE;
        } catch (ExecutionException e) {
            if(logger.isInfoEnabled()){
                logger.info(e.getMessage(), e);
            }
            
            response.status = Response.Status.FAILURE;
        }
        return response;
    }

    public Response.Count count(Request.Count request) throws RpcException {
        final String key = request.key;
        
        if(logger.isDebugEnabled()){
            logger.debug(new StringBuilder("count (")
                .append("key=").append(key)
                .append(")")
            );
        }
        
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
        
        if(logger.isDebugEnabled()){
            logger.debug(new StringBuilder("get (")
                .append("key=").append(key).append(",")
                .append("offset=").append(offset).append(",")
                .append("limit=").append(limit)
                .append(")")
            );
        }
        
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
        final boolean async = request.asyncRequest;
        
        if(logger.isDebugEnabled()){
            logger.debug(new StringBuilder("delete (")
                .append("key=").append(key).append(",")
                .append("expire=").append(expire).append(",")
                .append("async=").append(async)
                .append(")")
            );
        }
        
        final ListCommand command = new ListCommand(pooling);
        final Future<Boolean> future = command.delete(key, expire);
        
        if(async){
            Response.Delete response = Response.Delete.newInstance();
            response.status = Response.Status.ENQUEUE;
            return response;
        }
        
        //
        // sync request
        //
        Response.Delete response = Response.Delete.newInstance();
        try {
            Boolean status = future.get();
            
            if(status.booleanValue()){
                response.status = Response.Status.SUCCESS;
            } else {
                response.status = Response.Status.FAILURE;
            }
        } catch(InterruptedException e){
            if(logger.isInfoEnabled()){
                logger.info(e.getMessage(), e);
            }
            
            response.status = Response.Status.FAILURE;
        } catch(ExecutionException e){
            if(logger.isInfoEnabled()){
                logger.info(e.getMessage(), e);
            }
            
            response.status = Response.Status.FAILURE;
        }
        return response;
    }
    
    public Response.DeleteByValue deleteByValue(Request.DeleteByValue request) throws RpcException {
        final String key = request.key;
        final String value = request.value;
        final int expire = request.expire;
        final boolean async = request.asyncRequest;
        
        if(logger.isDebugEnabled()){
            logger.debug(new StringBuilder("deleteByValue (")
                .append("key=").append(key).append(",")
                .append("value=").append(value).append(",")
                .append("expire=").append(expire).append(",")
                .append("async=").append(async)
                .append(")")
            );
        }
        
        final ListCommand command = new ListCommand(pooling);
        final Future<Boolean> future = command.deleteAllValues(key, expire, value);
        if(async){
            Response.DeleteByValue response = Response.DeleteByValue.newInstance();
            response.status = Response.Status.ENQUEUE;
            return response;
        }
        
        //
        // sync request
        //
        Response.DeleteByValue response = Response.DeleteByValue.newInstance();
        try {
            Boolean success = future.get();
            if(success.booleanValue()){
                response.status = Response.Status.SUCCESS;
            } else {
                response.status = Response.Status.FAILURE;
            }
        } catch (InterruptedException e) {
            if(logger.isInfoEnabled()){
                logger.info(e.getMessage(), e);
            }
            
            response.status = Response.Status.FAILURE;
        } catch (ExecutionException e) {
            if(logger.isInfoEnabled()){
                logger.info(e.getMessage(), e);
            }
            
            response.status = Response.Status.FAILURE;
        }
        return response;
    }
    
    public Response.Reindex reindex(Request.Reindex request) throws RpcException {
        final String key = request.key;
        final boolean async = request.asyncRequest;
        
        if(logger.isDebugEnabled()){
            logger.debug(new StringBuilder("reindex (")
                .append("key=").append(key).append(",")
                .append("async=").append(async)
                .append(")")
            );
        }
        
        final ListCommand command = new ListCommand(pooling);
        final Future<Boolean> future = command.reindex(key);
        if(async){
            Response.Reindex response = Response.Reindex.newInstance();
            response.status = Response.Status.ENQUEUE;
            return response;
        }
        
        Response.Reindex response = Response.Reindex.newInstance();
        try {
            Boolean success = future.get();
            if(success.booleanValue()){
                response.status = Response.Status.SUCCESS;
            } else {
                response.status = Response.Status.FAILURE;
            }
        } catch (InterruptedException e) {
            if(logger.isInfoEnabled()){
                logger.info(e.getMessage(), e);
            }
            
            response.status = Response.Status.FAILURE;
        } catch (ExecutionException e) {
            if(logger.isInfoEnabled()){
                logger.info(e.getMessage(), e);
            }
            
            response.status = Response.Status.FAILURE;
        }
        return response;
    }

}
