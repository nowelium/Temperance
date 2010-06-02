package temperance.handler;

import libmemcached.exception.LibMemcachedException;
import temperance.memcached.Pool;
import temperance.protobuf.Map.MapService;
import temperance.protobuf.Map.Request;
import temperance.protobuf.Map.Response;
import temperance.storage.MemcachedMap;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

public class MapServiceHandler implements MapService.BlockingInterface {
    
    protected final Context context;
    
    protected final Pool pool;
    
    public MapServiceHandler(Context context, Pool pool){
        this.context = context;
        this.pool = pool;
    }
    
    public Response.Get get(RpcController controller, Request.Get request) throws ServiceException {
        final String key = request.getKey();
        
        MemcachedMap map = new MemcachedMap(pool);
        try {
            String result = map.get(key);
            Response.Get.Builder builder = Response.Get.newBuilder();
            builder.setValue(result);
            
            return builder.build();
        } catch (LibMemcachedException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    public Response.Set set(RpcController controller, Request.Set request) throws ServiceException {
        final String key = request.getKey();
        final String value = request.getValue();
        final int expire = request.getExpire();
        
        MemcachedMap map = new MemcachedMap(pool);
        try {
            map.set(key, value, expire);
            
            return Response.Set.newBuilder().setSucceed(true).build();
        } catch (LibMemcachedException e) {
            e.printStackTrace();
            return Response.Set.newBuilder().setSucceed(false).build();
        }
    }

}
