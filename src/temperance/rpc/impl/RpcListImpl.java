package temperance.rpc.impl;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import libmemcached.exception.LibMemcachedException;
import temperance.exception.RpcException;
import temperance.memcached.ConnectionPool;
import temperance.memcached.ListCommand;
import temperance.rpc.Context;
import temperance.rpc.RpcList;
import temperance.storage.MemcachedList;

public class RpcListImpl implements RpcList {
    
    protected final Context context;
    
    protected final ConnectionPool pool;
    
    protected final MemcachedList list;
    
    public RpcListImpl(Context context, ConnectionPool pool){
        this.context = context;
        this.pool = pool;
        this.list = new MemcachedList(pool);
    }

    public Response.Add add(Request.Add request) throws RpcException {
        final String key = request.key;
        final String value = request.value;
        final int expire = request.expire;
        
        try {
            list.add(key, value, expire);
            Response.Add response = Response.Add.newInstance();
            response.succeed = true;
            return response;
        } catch(LibMemcachedException e){
            Response.Add response = Response.Add.newInstance();
            response.succeed = false;
            return response;
        }
    }

    public Response.Count count(Request.Count request) throws RpcException {
        final String key = request.key;
        
        try {
            long count = list.count(key);
            Response.Count response = Response.Count.newInstance();
            response.count = count;
            return response;
        } catch(LibMemcachedException e){
            throw new RpcException(e);
        }
    }

    public Response.Get get(Request.Get request) throws RpcException {
        final String key = request.key;
        final long offset = request.offset;
        long limit = request.limit;
        
        try {
            long count = list.count(key);
            if(count < limit){
                limit = count;
            }
            
            ListCommand command = new ListCommand(pool);
            Future<List<String>> future = command.get(key, offset, limit);
            
            Response.Get response = Response.Get.newInstance();
            response.values = future.get();
            return response;
        } catch(LibMemcachedException e){
            throw new RpcException(e);
        } catch (InterruptedException e) {
            throw new RpcException(e);
        } catch (ExecutionException e) {
            throw new RpcException(e);
        }
    }

}
