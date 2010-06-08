package temperance.rpc.msgpack;

import java.util.List;

import temperance.exception.RpcException;
import temperance.rpc.RpcQuery;

public class MsgpackQueryService {

    protected final RpcQuery rpc;
    
    public MsgpackQueryService(RpcQuery rpc){
        this.rpc = rpc;
    }
    
    public List<String> select(String query) throws RpcException {
        RpcQuery.Request.Select request = RpcQuery.Request.Select.newInstance();
        request.query = query;
        
        RpcQuery.Response.Select response = rpc.select(request);
        return response.values;
    }
    
    public List<String> delete(String query) throws RpcException {
        RpcQuery.Request.Delete request = RpcQuery.Request.Delete.newInstance();
        request.query = query;
        
        RpcQuery.Response.Delete response = rpc.delete(request);
        return response.values;
    }
}
