package temperance.rpc.msgpack;

import java.util.List;

import temperance.exception.RpcException;
import temperance.rpc.RpcFullText;

public class MsgpackFullTextService {
    
    protected final RpcFullText rpc;
    
    public MsgpackFullTextService(RpcFullText rpc){
        this.rpc = rpc;
    }
    
    public List<String> search(String key, String str) throws RpcException {
        return search(key, str, RpcFullText.Request.Parser.MECAB.getValue());
    }
    
    public List<String> search(String key, String str, int parser) throws RpcException {
        RpcFullText.Request.Search request = RpcFullText.Request.Search.newInstance();
        request.key = key;
        request.str = str;
        request.parser = RpcFullText.Request.Parser.get(parser);
        
        RpcFullText.Response.Search response = rpc.search(request);
        return response.values;
    }
    
    public boolean set(String key, String str, String value) throws RpcException {
        return set(key, str, value, RpcFullText.Request.Set.DEFAULT_EXPIRE);
    }
    
    public boolean set(String key, String str, String value, int expire) throws RpcException {
        return set(key, str, value, expire, RpcFullText.Request.Parser.MECAB.getValue());
    }
    
    public boolean set(String key, String str, String value, int expire, int parser) throws RpcException {
        RpcFullText.Request.Set request = RpcFullText.Request.Set.newInstance();
        request.key = key;
        request.str = str;
        request.value = value;
        request.expire = expire;
        request.parser = RpcFullText.Request.Parser.get(parser);
        
        RpcFullText.Response.Set response = rpc.set(request);
        return response.succeed;
    }
    
}
