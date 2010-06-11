package temperance.rpc.impl;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import libmemcached.exception.LibMemcachedException;
import temperance.core.Configure;
import temperance.core.ListCommand;
import temperance.core.Pooling;
import temperance.exception.RpcException;
import temperance.rpc.RpcList;
import temperance.storage.MemcachedList;

public class RpcListImpl implements RpcList {
    
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
        } catch(LibMemcachedException e){
            Response.Add response = Response.Add.newInstance();
            response.succeed = false;
            return response;
        }
    }

    public Response.Count count(Request.Count request) throws RpcException {
        final String key = request.key;
        
        final MemcachedList list = new MemcachedList(pooling.getConnectionPool());
        try {
            final long count = list.count(key);
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

}
