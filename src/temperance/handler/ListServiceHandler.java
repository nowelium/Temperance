package temperance.handler;

import java.util.List;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.MemcachedClient;
import temperance.protobuf.List.ListService;
import temperance.protobuf.List.Request;
import temperance.protobuf.List.Response;
import temperance.storage.MemcachedList;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

public class ListServiceHandler implements ListService.BlockingInterface {
    
    protected final Context context;
    
    public ListServiceHandler(Context context){
        this.context = context;
    }

    public Response.Get get(RpcController controller, Request.Get request) throws ServiceException {
        String namespace = request.getNamespace();
        String key = request.getKey();
        long offset = request.getOffset();
        long limit = request.getLimit();
        MemcachedClient client = new MemcachedClient();
        client.addServer(context.getHost(), context.getPort());
        
        MemcachedList list = new MemcachedList(client, namespace);
        try {
            Response.Get.Builder builder = Response.Get.newBuilder();
            List<String> values = list.get(key, offset, limit);
            builder.addAllValues(values);
            return builder.build();
        } catch(LibMemcachedException e){
            throw new ServiceException(e.getMessage());
        }
    }

    public Response.Set set(RpcController controller, Request.Set request) throws ServiceException {
        String namespace = request.getNamespace();
        String key = request.getKey();
        String value = request.getValue();
        MemcachedClient client = new MemcachedClient();
        client.addServer(context.getHost(), context.getPort());
        
        MemcachedList list = new MemcachedList(client, namespace);
        try {
            list.add(key, value);
            return Response.Set.newBuilder().setSucceed(true).build();
        } catch(LibMemcachedException e){
            //throw new ServiceException(e.getMessage());
            return Response.Set.newBuilder().setSucceed(false).build();
        }
    }

    public Response.Count count(RpcController controller, Request.Count request) throws ServiceException {
        String namespace = request.getNamespace();
        String key = request.getKey();
        
        MemcachedClient client = new MemcachedClient();
        client.addServer(context.getHost(), context.getPort());
        
        MemcachedList list = new MemcachedList(client, namespace);
        try {
            long count = list.count(key);
            return Response.Count.newBuilder().setCount(count).build();
        } catch(LibMemcachedException e){
            throw new ServiceException(e.getMessage());
        }
    }

}
