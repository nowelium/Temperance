package temperance.rpc.msgpack;

import java.io.IOException;
import java.util.List;

import org.msgpack.MessagePackable;
import org.msgpack.Packer;

import temperance.exception.RpcException;
import temperance.rpc.RpcMecab;
import temperance.util.Lists;

public class MsgpackMecabService {
    
    protected final RpcMecab rpc;
    
    public MsgpackMecabService(RpcMecab rpc){
        this.rpc = rpc;
    }
    
    public List<Node> parse(String str) throws RpcException {
        RpcMecab.Request.Parse request = RpcMecab.Request.Parse.newInstance();
        request.str = str;
        
        RpcMecab.Response.Parse response = rpc.parse(request);
        
        List<Node> nodes = Lists.newArrayList();
        for(RpcMecab.Response.Node responseNode: response.nodes){
            Node node = new Node();
            node.surface = responseNode.surface;
            node.feature = responseNode.feature;
            nodes.add(node);
        }
        return nodes;
    }
    
    public static class Node implements MessagePackable {
        public String surface;
        public String feature;
        public void messagePack(Packer packer) throws IOException {
            packer.packArray(2);
            packer.packString(surface);
            packer.packString(feature);
        }
    }
    
}
