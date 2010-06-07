package temperance.rpc.protobuf;

import temperance.exception.RpcException;
import temperance.protobuf.List.ListService;
import temperance.protobuf.List.Request;
import temperance.protobuf.List.Response;
import temperance.rpc.RpcList;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

public class ProtobufListService implements ListService.BlockingInterface {
    
    protected final RpcList rpc;
    
    public ProtobufListService(RpcList rpc){
        this.rpc = rpc;
    }

    public Response.Add add(RpcController controller, Request.Add add) throws ServiceException {
        RpcList.Request.Add request = RpcList.Request.Add.newInstance();
        request.key = add.getKey();
        request.value = add.getValue();
        request.expire = add.getExpire();
        
        try {
            RpcList.Response.Add response = rpc.add(request);
            
            Response.Add.Builder builder = Response.Add.newBuilder();
            builder.setSucceed(response.succeed);
            return builder.build();
        } catch(RpcException e){
            throw new ServiceException(e.getMessage());
        }
    }

    public Response.Get get(RpcController controller, Request.Get get) throws ServiceException {
        RpcList.Request.Get request = RpcList.Request.Get.newInstance();
        request.key = get.getKey();
        request.offset = get.getOffset();
        request.limit = get.getLimit();
        
        try {
            RpcList.Response.Get response = rpc.get(request);
            
            Response.Get.Builder builder = Response.Get.newBuilder();
            builder.addAllValues(response.values);
            return builder.build();
        } catch(RpcException e){
            throw new ServiceException(e.getMessage());
        }
    }
    
    public Response.Count count(RpcController controller, Request.Count count) throws ServiceException {
        RpcList.Request.Count request = RpcList.Request.Count.newInstance();
        request.key = count.getKey();
        
        try {
            RpcList.Response.Count response = rpc.count(request);
            
            Response.Count.Builder builder = Response.Count.newBuilder();
            builder.setCount(response.count);
            return builder.build();
        } catch(RpcException e){
            throw new ServiceException(e.getMessage());
        }
    }

}
