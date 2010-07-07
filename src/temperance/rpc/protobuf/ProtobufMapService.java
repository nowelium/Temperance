package temperance.rpc.protobuf;

import java.util.List;

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
            if(null == response.value){
                return Response.Get.newBuilder().clear().buildPartial();
            }
            return Response.Get.newBuilder().setValue(response.value).build();
        } catch(RpcException e){
            throw new ServiceException(e.getMessage());
        }
    }
    
    public Response.GetValues getValues(RpcController controller, Request.GetValues getValues) throws ServiceException {
        RpcMap.Request.GetValues request = RpcMap.Request.GetValues.newInstance();
        request.keys = getValues.getKeysList();
        
        try {
            RpcMap.Response.GetValues response = rpc.getValues(request);
            List<RpcMap.Response.Entry> entries = response.values;
            
            Response.GetValues.Builder builder = Response.GetValues.newBuilder();
            for(RpcMap.Response.Entry entry: entries){
                Response.Entry.Builder responseEntryBuilder = Response.Entry.newBuilder();
                responseEntryBuilder.setKey(entry.key);
                responseEntryBuilder.setValue(entry.value);
                builder.addValues(responseEntryBuilder.build());
            }
            return builder.build();
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
    
    public Response.Delete delete(RpcController controller, Request.Delete delete) throws ServiceException {
        RpcMap.Request.Delete request = RpcMap.Request.Delete.newInstance();
        request.key = delete.getKey();
        request.expire = delete.getExpire();
        
        try {
            RpcMap.Response.Delete response = rpc.delete(request);
            return Response.Delete.newBuilder().setSucceed(response.succeed).build();
        } catch(RpcException e){
            throw new ServiceException(e.getMessage());
        }
    }

}
