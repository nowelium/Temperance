package temperance.handler;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.MemcachedClient;
import temperance.protobuf.Map.MapService;
import temperance.protobuf.Map.Request;
import temperance.protobuf.Map.Response;
import temperance.storage.MemcachedMap;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

public class MapServiceHandler implements MapService.BlockingInterface {
    
    protected final Context context;
    
    public MapServiceHandler(Context context){
        this.context = context;
    }

    public Response.Get get(RpcController controller, Request.Get request) throws ServiceException {
        String namespace = request.getNamespace();
        String key = request.getKey();
        MemcachedClient client = new MemcachedClient();
        client.addServer(context.getHost(), context.getPort());
        
        MemcachedMap map = new MemcachedMap(client, namespace);
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
        String namespace = request.getNamespace();
        String key = request.getKey();
        String value = request.getValue();
        MemcachedClient client = new MemcachedClient();
        client.addServer(context.getHost(), context.getPort());
        
        MemcachedMap map = new MemcachedMap(client, namespace);
        try {
            map.set(key, value);
            
            return Response.Set.newBuilder().setSucceed(true).build();
        } catch (LibMemcachedException e) {
            return Response.Set.newBuilder().setSucceed(false).build();
        }
    }

}
