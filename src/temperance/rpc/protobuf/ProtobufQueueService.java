package temperance.rpc.protobuf;

import temperance.exception.RpcException;
import temperance.protobuf.Queue.QueueService;
import temperance.protobuf.Queue.Request;
import temperance.protobuf.Queue.Response;
import temperance.rpc.RpcQueue;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

public class ProtobufQueueService implements QueueService.BlockingInterface {

    protected final RpcQueue rpc;
    
    public ProtobufQueueService(RpcQueue rpc){
        this.rpc = rpc;
    }
    
    public Response.Enqueue enqueue(RpcController controller, Request.Enqueue enqueue) throws ServiceException {
        RpcQueue.Request.Enqueue request = RpcQueue.Request.Enqueue.newInstance();
        request.key = enqueue.getKey();
        request.value = enqueue.getValue();
        request.expire = enqueue.getExpire();
        
        try {
            RpcQueue.Response.Enqueue response = rpc.enqueue(request);
            
            Response.Enqueue.Builder builder = Response.Enqueue.newBuilder();
            builder.setSucceed(response.succeed);
            return builder.build();
        } catch(RpcException e){
            throw new ServiceException(e.getMessage());
        }
    }
    
    public Response.Dequeue dequeue(RpcController controller, Request.Dequeue dequeue) throws ServiceException {
        RpcQueue.Request.Dequeue request = RpcQueue.Request.Dequeue.newInstance();
        request.key = dequeue.getKey();
        request.timeout = dequeue.getTimeout();
        
        try {
            RpcQueue.Response.Dequeue response = rpc.dequeue(request);
            if(null == response.value){
                return Response.Dequeue.newBuilder().clear().buildPartial();
            }
            
            Response.Dequeue.Builder builder = Response.Dequeue.newBuilder();
            builder.setValue(response.value);
            return builder.build();
        } catch(RpcException e){
            throw new ServiceException(e.getMessage());
        }
    }
    
}
