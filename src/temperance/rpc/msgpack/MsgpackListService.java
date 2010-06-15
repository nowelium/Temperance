package temperance.rpc.msgpack;

import java.util.List;

import temperance.exception.RpcException;
import temperance.rpc.RpcList;

public class MsgpackListService {

    protected final RpcList rpc;
    
    public MsgpackListService(RpcList rpc){
        this.rpc = rpc;
    }
    
    public boolean add(String key, String value) throws RpcException {
        return add(key, value, RpcList.Request.Add.DEFAULT_EXPIRE);
    }
    
    public boolean add(String key, String value, int expire) throws RpcException {
        RpcList.Request.Add request = RpcList.Request.Add.newInstance();
        request.key = key;
        request.value = value;
        request.expire = expire;
        
        RpcList.Response.Add response = rpc.add(request);
        return response.succeed;
    }
    
    public List<String> get(String key) throws RpcException {
        return get(key, RpcList.Request.Get.DEFAULT_OFFSET, RpcList.Request.Get.DEFAULT_LIMIT);
    }
    
    public List<String> get(String key, long offset, long limit) throws RpcException {
        RpcList.Request.Get request = RpcList.Request.Get.newInstance();
        request.key = key;
        request.offset = offset;
        request.limit = limit;
        
        RpcList.Response.Get response = rpc.get(request);
        return response.values;
    }
    
    public long count(String key) throws RpcException {
        RpcList.Request.Count request = RpcList.Request.Count.newInstance();
        request.key = key;
        
        RpcList.Response.Count response = rpc.count(request);
        return response.count;
    }
    
    public boolean delete(String key) throws RpcException {
        return delete(key, RpcList.Request.Delete.DEFAULT_EXPIRE);
    }
    
    public boolean delete(String key, int expire) throws RpcException {
        RpcList.Request.Delete request = RpcList.Request.Delete.newInstance();
        request.key = key;
        request.expire = expire;
        
        RpcList.Response.Delete response = rpc.delete(request);
        return response.succeed;
    }
}
