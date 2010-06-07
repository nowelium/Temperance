package temperance.rpc.impl;

import libmemcached.exception.LibMemcachedException;
import temperance.exception.RpcException;
import temperance.memcached.ConnectionPool;
import temperance.rpc.Context;
import temperance.rpc.RpcMap;
import temperance.storage.MemcachedMap;

public class RpcMapImpl implements RpcMap {
    
    protected final Context context;
    
    protected final ConnectionPool pool;
    
    protected final MemcachedMap map;
    
    public RpcMapImpl(Context context, ConnectionPool pool){
        this.context = context;
        this.pool = pool;
        this.map = new MemcachedMap(pool);
    }

    public Response.Get get(Request.Get request) throws RpcException {
        final String key = request.key;
        
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
        
        try {
            map.set(key, value, expire);
            
            Response.Set response = Response.Set.newInstance();
            response.succeed = true;
            return response;
        } catch (LibMemcachedException e) {
            Response.Set response = Response.Set.newInstance();
            response.succeed = false;
            return response;
        }
    }

}
