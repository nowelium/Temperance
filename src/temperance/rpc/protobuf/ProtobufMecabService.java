package temperance.rpc.protobuf;

import temperance.exception.RpcException;
import temperance.protobuf.Mecab.MecabService;
import temperance.protobuf.Mecab.Request;
import temperance.protobuf.Mecab.Response;
import temperance.rpc.RpcMecab;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

public class ProtobufMecabService implements MecabService.BlockingInterface {
    
    protected final RpcMecab rpc;
    
    public ProtobufMecabService(RpcMecab rpc){
        this.rpc = rpc;
    }

    public Response.Parse parse(RpcController controller, Request.Parse parse) throws ServiceException {
        RpcMecab.Request.Parse request = RpcMecab.Request.Parse.newInstance();
        request.str = parse.getStr();
        
        try {
            RpcMecab.Response.Parse response = rpc.parse(request);
            
            Response.Parse.Builder builder = Response.Parse.newBuilder();
            for(RpcMecab.Response.Node node: response.nodes){
                Response.Node.Builder nodeBuilder = Response.Node.newBuilder();
                nodeBuilder.setSurface(node.surface);
                nodeBuilder.setFeature(node.feature);
                builder.addNodes(nodeBuilder);
            }
            return builder.build();
        } catch(RpcException e){
            throw new ServiceException(e.getMessage());
        }
    }

}
