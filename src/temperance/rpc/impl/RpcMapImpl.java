package temperance.rpc.impl;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import temperance.core.Configure;
import temperance.core.MapCommand;
import temperance.core.Pooling;
import temperance.exception.MemcachedOperationException;
import temperance.exception.RpcException;
import temperance.rpc.RpcMap;
import temperance.storage.TpMap;
import temperance.storage.TpMap.TpMapResult;
import temperance.storage.impl.MemcachedMap;

public class RpcMapImpl implements RpcMap {
    
    protected static final Log logger = LogFactory.getLog(RpcMapImpl.class);
    
    protected final Configure context;
    
    protected final Pooling pooling;
    
    public RpcMapImpl(Configure context, Pooling pooling){
        this.context = context;
        this.pooling = pooling;
    }

    public Response.Get get(Request.Get request) throws RpcException {
        final String key = request.key;
        
        if(logger.isDebugEnabled()){
            logger.debug(new StringBuilder("get (")
                .append("key=").append(key)
                .append(")")
            );
        }
        
        final TpMap map = new MemcachedMap(pooling.getConnectionPool());
        try {
            String result = map.get(key);
            
            Response.Get response = Response.Get.newInstance();
            response.value = result;
            return response;
        } catch (MemcachedOperationException e) {
            throw new RpcException(e);
        }
    }
    
    public Response.GetValues getValues(Request.GetValues request) throws RpcException {
        final List<String> keys = request.keys;
        
        if(logger.isDebugEnabled()){
            logger.debug(new StringBuilder("getValues (")
                .append("keys=").append(keys)
                .append(")")
            );
        }
        
        final MapCommand command = new MapCommand(pooling);
        try {
            List<TpMapResult> results = command.getValues(keys);
            
            Response.GetValues response = Response.GetValues.newInstance();
            for(TpMapResult result: results){
                response.add(result.getKey(), result.getValue());
            }
            return response;
        } catch (InterruptedException e) {
            throw new RpcException(e);
        } catch (ExecutionException e) {
            throw new RpcException(e);
        }
    }

    public Response.Set set(Request.Set request) throws RpcException {
        final String key = request.key;
        final String value = request.value;
        final int expire = request.expire;
        
        if(logger.isDebugEnabled()){
            logger.debug(new StringBuilder("set (")
                .append("key=").append(key).append(",")
                .append("value=").append(value).append(",")
                .append("expire=").append(expire)
                .append(")")
            );
        }
        
        final TpMap map = new MemcachedMap(pooling.getConnectionPool());
        boolean success = map.set(key, value, expire);
        
        Response.Set response = Response.Set.newInstance();
        response.succeed = success;
        return response;
    }

    public Response.Delete delete(Request.Delete request) throws RpcException {
        final String key = request.key;
        final int expire = request.expire;
        
        if(logger.isDebugEnabled()){
            logger.debug(new StringBuilder("delete (")
                .append("key=").append(key).append(",")
                .append("expire=").append(expire)
                .append(")")
            );
        }
        
        final TpMap map = new MemcachedMap(pooling.getConnectionPool());
        boolean success = map.delete(key, expire);
        
        Response.Delete response = Response.Delete.newInstance();
        response.succeed = success;
        return response;
    }

}
