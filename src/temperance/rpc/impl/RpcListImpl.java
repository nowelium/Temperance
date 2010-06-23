package temperance.rpc.impl;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import libmemcached.exception.LibMemcachedException;
import temperance.core.Configure;
import temperance.core.SequenceCommand;
import temperance.core.Pooling;
import temperance.exception.RpcException;
import temperance.rpc.RpcList;
import temperance.storage.impl.MemcachedSequence;

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
        
        final MemcachedSequence list = new MemcachedSequence(pooling.getConnectionPool());
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
        
        final MemcachedSequence list = new MemcachedSequence(pooling.getConnectionPool());
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
        final long limit = request.limit;
        
        final SequenceCommand command = new SequenceCommand(pooling);
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
        
        final MemcachedSequence list = new MemcachedSequence(pooling.getConnectionPool());
        try {
            boolean success = list.delete(key, expire);
            
            Response.Delete response = Response.Delete.newInstance();
            response.succeed = success;
            return response;
        } catch(LibMemcachedException e){
            Response.Delete response = Response.Delete.newInstance();
            response.succeed = false;
            return response;
        }
    }
    
    public void deleteByValue(){
        
    }

}
