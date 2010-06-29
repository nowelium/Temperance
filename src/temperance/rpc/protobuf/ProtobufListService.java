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
    
    protected static Response.Status convert(RpcList.Response.Status status){
        switch(status){
        case SUCCESS:
            return Response.Status.SUCCESS;
        case ENQUEUE:
            return Response.Status.ENQUEUE;
        case FAILURE:
            return Response.Status.FAILURE;
        case TIMEOUT:
            return Response.Status.TIMEOUT;
        }
        throw new RuntimeException("unknown status:" + status);
    }

    public Response.Add add(RpcController controller, Request.Add add) throws ServiceException {
        RpcList.Request.Add request = RpcList.Request.Add.newInstance();
        request.key = add.getKey();
        request.value = add.getValue();
        request.expire = add.getExpire();
        
        try {
            RpcList.Response.Add response = rpc.add(request);
            
            Response.Add.Builder builder = Response.Add.newBuilder();
            builder.setStatus(convert(response.status));
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
    
    public Response.Delete delete(RpcController controller, Request.Delete delete) throws ServiceException {
        RpcList.Request.Delete request = RpcList.Request.Delete.newInstance();
        request.key = delete.getKey();
        request.expire = delete.getExpire();
        
        try {
            RpcList.Response.Delete response = rpc.delete(request);
            
            Response.Delete.Builder builder = Response.Delete.newBuilder();
            builder.setStatus(convert(response.status));
            return builder.build();
        } catch(RpcException e){
            throw new ServiceException(e.getMessage());
        }
    }
    
    public Response.DeleteByValue deleteByValue(RpcController controller, Request.DeleteByValue deleteByValue) throws ServiceException {
        RpcList.Request.DeleteByValue request = RpcList.Request.DeleteByValue.newInstance();
        request.key = deleteByValue.getKey();
        request.value = deleteByValue.getValue();
        request.expire = deleteByValue.getExpire();
        
        try {
            RpcList.Response.DeleteByValue response = rpc.deleteByValue(request);
            
            Response.DeleteByValue.Builder builder = Response.DeleteByValue.newBuilder();
            builder.setStatus(convert(response.status));
            return builder.build();
        } catch(RpcException e){
            throw new ServiceException(e.getMessage());
        }
    }
    
    public Response.Reindex reindex(RpcController controller, Request.Reindex reindex) throws ServiceException {
        RpcList.Request.Reindex request = RpcList.Request.Reindex.newInstance();
        request.key = reindex.getKey();
        
        try {
            RpcList.Response.Reindex response = rpc.reindex(request);
            
            Response.Reindex.Builder builder = Response.Reindex.newBuilder();
            builder.setStatus(convert(response.status));
            return builder.build();
        } catch(RpcException e){
            throw new ServiceException(e.getMessage());
        }
        
    }

}
