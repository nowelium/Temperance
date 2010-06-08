package temperance.rpc.msgpack;

import temperance.exception.RpcException;
import temperance.rpc.RpcMap;
import temperance.rpc.RpcMap.Request;
import temperance.rpc.RpcMap.Response;

public class MsgpackMapService {

    protected final RpcMap rpc;
    
    public MsgpackMapService(RpcMap rpc){
        this.rpc = rpc;
    }
    
    public boolean set(String key, String value) throws RpcException {
        return set(key, value, Request.Set.DEFAULT_EXPIRE);
    }
    
    public boolean set(String key, String value, int expire) throws RpcException {
        Request.Set request = Request.Set.newInstance();
        request.key = key;
        request.value = value;
        request.expire = expire;
        
        Response.Set response = rpc.set(request);
        return response.succeed;
    }
    
    public String get(String key) throws RpcException {
        Request.Get request = Request.Get.newInstance();
        request.key = key;
        
        Response.Get response = rpc.get(request);
        return response.value;
    }
}
