package temperance.rpc.protobuf;

import temperance.exception.RpcException;
import temperance.protobuf.Temperance.Request;
import temperance.protobuf.Temperance.Response;
import temperance.protobuf.Temperance.TemperanceService;
import temperance.rpc.RpcTemperance;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

public class ProtobufTemperanceService implements TemperanceService.BlockingInterface {

    protected final RpcTemperance rpc;
    
    public ProtobufTemperanceService(RpcTemperance rpc){
        this.rpc = rpc;
    }
    
    public Response.Monitor monitor(RpcController controller, Request.Monitor request) throws ServiceException {
        try {
            RpcTemperance.Response.Monitor response = rpc.monitor();
            
            Response.Monitor.Builder builder = Response.Monitor.newBuilder();
            builder.setInProgressTasks(response.inProgressTasks);
            builder.setQueuedTasks(response.queuedTasks);
            builder.setTotalTasks(response.totalTasks);
            builder.setTotalTime(response.totalTime);
            return builder.build();
        } catch(RpcException e){
            throw new ServiceException(e.getMessage());
        }
    }

}
