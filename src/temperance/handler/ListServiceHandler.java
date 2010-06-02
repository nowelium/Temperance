package temperance.handler;

import java.util.List;

import libmemcached.exception.LibMemcachedException;
import temperance.memcached.Pool;
import temperance.protobuf.List.ListService;
import temperance.protobuf.List.Request;
import temperance.protobuf.List.Response;
import temperance.storage.MemcachedList;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

public class ListServiceHandler implements ListService.BlockingInterface {
    
    protected final Context context;
    
    protected final Pool pool;
    
    protected final MemcachedList list;
    
    public ListServiceHandler(Context context, Pool pool){
        this.context = context;
        this.pool = pool;
        this.list = new MemcachedList(pool);
    }
    
    public Response.Get get(RpcController controller, Request.Get request) throws ServiceException {
        final String key = request.getKey();
        final long offset = request.getOffset();
        long limit = request.getLimit();
        
        try {
            long count = list.count(key);
            if(count < limit){
                limit = count;
            }
            
            List<String> values = list.get(key, offset, limit);
            return Response.Get.newBuilder().addAllValues(values).build();
        } catch(LibMemcachedException e){
            throw new ServiceException(e.getMessage());
        }
    }

    public Response.Add add(RpcController controller, Request.Add request) throws ServiceException {
        final String key = request.getKey();
        final String value = request.getValue();
        final int expire = request.getExpire();
        
        try {
            list.add(key, value, expire);
            return Response.Add.newBuilder().setSucceed(true).build();
        } catch(LibMemcachedException e){
            e.printStackTrace();
            return Response.Add.newBuilder().setSucceed(false).build();
        }
    }

    public Response.Count count(RpcController controller, Request.Count request) throws ServiceException {
        final String key = request.getKey();
        
        try {
            long count = list.count(key);
            return Response.Count.newBuilder().setCount(count).build();
        } catch(LibMemcachedException e){
            throw new ServiceException(e.getMessage());
        }
    }

}
