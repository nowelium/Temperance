package temperance.rpc.protobuf;

import temperance.exception.RpcException;
import temperance.protobuf.Query.QueryService;
import temperance.protobuf.Query.Request;
import temperance.protobuf.Query.Response;
import temperance.rpc.RpcQuery;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

public class ProtobufQueryService implements QueryService.BlockingInterface {
    
    protected final RpcQuery rpc;
    
    public ProtobufQueryService(RpcQuery rpc){
        this.rpc = rpc;
    }

    public Response.Delete delete(RpcController controller, Request.Delete delete) throws ServiceException {
        RpcQuery.Request.Delete request = RpcQuery.Request.Delete.newInstance();
        request.query = delete.getQuery();
        
        try {
            RpcQuery.Response.Delete response = rpc.delete(request);
            
            Response.Delete.Builder builder = Response.Delete.newBuilder();
            builder.addAllValues(response.values);
            return builder.build();
        } catch(RpcException e){
            throw new ServiceException(e.getMessage());
        }
    }

    public Response.Select select(RpcController controller, Request.Select select) throws ServiceException {
        RpcQuery.Request.Select request = RpcQuery.Request.Select.newInstance();
        request.query = select.getQuery();
        
        try {
            RpcQuery.Response.Select response = rpc.select(request);
            
            Response.Select.Builder builder = Response.Select.newBuilder();
            builder.addAllValues(response.values);
            return builder.build();
        } catch(RpcException e){
            throw new ServiceException(e.getMessage());
        }
    }

}
