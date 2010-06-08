package temperance.rpc.protobuf;

import temperance.exception.RpcException;
import temperance.protobuf.Map.MapService;
import temperance.protobuf.Map.Request;
import temperance.protobuf.Map.Response;
import temperance.rpc.RpcMap;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

public class ProtobufMapService implements MapService.BlockingInterface {
    
    protected final RpcMap rpc;
    
    public ProtobufMapService(RpcMap rpc){
        this.rpc = rpc;
    }

    public Response.Get get(RpcController controller, Request.Get get) throws ServiceException {
        RpcMap.Request.Get request = RpcMap.Request.Get.newInstance();
        request.key = get.getKey();
        
        try {
            RpcMap.Response.Get response = rpc.get(request);
            return Response.Get.newBuilder().setValue(response.value).build();
        } catch(RpcException e){
            throw new ServiceException(e.getMessage());
        }
    }

    public Response.Set set(RpcController controller, Request.Set set) throws ServiceException {
        RpcMap.Request.Set request = RpcMap.Request.Set.newInstance();
        request.key = set.getKey();
        request.value = set.getValue();
        request.expire = set.getExpire();
        
        try {
            RpcMap.Response.Set response = rpc.set(request);
            return Response.Set.newBuilder().setSucceed(response.succeed).build();
        } catch(RpcException e){
            throw new ServiceException(e.getMessage());
        }
    }

}
