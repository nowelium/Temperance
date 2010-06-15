package temperance.rpc.impl;

import libmemcached.exception.LibMemcachedException;
import temperance.core.Configure;
import temperance.core.Pooling;
import temperance.exception.RpcException;
import temperance.rpc.RpcMap;
import temperance.storage.MemcachedMap;

public class RpcMapImpl implements RpcMap {
    
    protected final Configure context;
    
    protected final Pooling pooling;
    
    public RpcMapImpl(Configure context, Pooling pooling){
        this.context = context;
        this.pooling = pooling;
    }

    public Response.Get get(Request.Get request) throws RpcException {
        final String key = request.key;
        
        final MemcachedMap map = new MemcachedMap(pooling.getConnectionPool());
        try {
            String result = map.get(key);
            
            Response.Get response = Response.Get.newInstance();
            response.value = result;
            return response;
        } catch (LibMemcachedException e) {
            throw new RpcException(e);
        }
    }

    public Response.Set set(Request.Set request) throws RpcException {
        final String key = request.key;
        final String value = request.value;
        final int expire = request.expire;
        
        final MemcachedMap map = new MemcachedMap(pooling.getConnectionPool());
        try {
            boolean success = map.set(key, value, expire);
            
            Response.Set response = Response.Set.newInstance();
            response.succeed = success;
            return response;
        } catch (LibMemcachedException e) {
            Response.Set response = Response.Set.newInstance();
            response.succeed = false;
            return response;
        }
    }

    public Response.Delete delete(Request.Delete request) throws RpcException {
        final String key = request.key;
        final int expire = request.expire;
        
        final MemcachedMap map = new MemcachedMap(pooling.getConnectionPool());
        try {
            boolean success = map.delete(key, expire);
            
            Response.Delete response = Response.Delete.newInstance();
            response.succeed = success;
            return response;
        } catch(LibMemcachedException e){
            Response.Delete response = Response.Delete.newInstance();
            response.succeed = false;
            return response;
        }
    }

}
