package temperance.rpc.protobuf;

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
        return null;
    }

    public Response.Set set(RpcController controller, Request.Set set) throws ServiceException {
        return null;
    }

}
