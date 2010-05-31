package temperance.handler;

import java.util.List;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.MemcachedServerList;
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
    
    protected MemcachedClient createMemcachedClient(){
        MemcachedClient client = new MemcachedClient();
        MemcachedServerList servers = client.getServerList();
        servers.parse(context.getMemcached());
        servers.push();
        return client;
    }

    public Response.Get get(RpcController controller, Request.Get request) throws ServiceException {
        final String key = request.getKey();
        final long offset = request.getOffset();
        final long limit = request.getLimit();
        MemcachedClient client = createMemcachedClient();
        
        MemcachedList list = new MemcachedList(client);
        try {
            Response.Get.Builder builder = Response.Get.newBuilder();
            List<String> values = list.get(key, offset, limit);
            builder.addAllValues(values);
            return builder.build();
        } catch(LibMemcachedException e){
            throw new ServiceException(e.getMessage());
        }
    }

    public Response.Add add(RpcController controller, Request.Add request) throws ServiceException {
        final String key = request.getKey();
        final String value = request.getValue();
        final int expire = request.getExpire();
        MemcachedClient client = createMemcachedClient();
        
        MemcachedList list = new MemcachedList(client);
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
        MemcachedClient client = createMemcachedClient();
        
        MemcachedList list = new MemcachedList(client);
        try {
            long count = list.count(key);
            return Response.Count.newBuilder().setCount(count).build();
        } catch(LibMemcachedException e){
            throw new ServiceException(e.getMessage());
        }
    }

}
