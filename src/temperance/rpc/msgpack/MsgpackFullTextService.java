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
    
    public int set(String key, String str, String value) throws RpcException {
        return set(key, str, value, RpcFullText.Request.Add.DEFAULT_EXPIRE);
    }
    
    public int set(String key, String str, String value, int expire) throws RpcException {
        return set(key, str, value, expire, RpcFullText.Request.Parser.MECAB.getValue());
    }
    
    public int set(String key, String str, String value, int expire, int parser) throws RpcException {
        RpcFullText.Request.Add request = RpcFullText.Request.Add.newInstance();
        request.key = key;
        request.str = str;
        request.value = value;
        request.expire = expire;
        request.parser = RpcFullText.Request.Parser.get(parser);
        
        RpcFullText.Response.Add response = rpc.add(request);
        return response.status.getValue();
    }
    
    public int delete(String key) throws RpcException {
        return delete(key, RpcFullText.Request.Delete.DEFAULT_EXPIRE);
    }
    
    public int delete(String key, int expire) throws RpcException {
        RpcFullText.Request.Delete request = RpcFullText.Request.Delete.newInstance();
        request.key = key;
        request.expire = expire;
        
        RpcFullText.Response.Delete response = rpc.delete(request);
        return response.status.getValue();
    }
    
    public int deleteByValue(String key, String value) throws RpcException {
        return deleteByValue(key, value, RpcFullText.Request.DeleteByValue.DEFAULT_EXPIRE);
    }
    
    public int deleteByValue(String key, String value, int expire) throws RpcException {
        RpcFullText.Request.DeleteByValue request = RpcFullText.Request.DeleteByValue.newInstance();
        request.key = key;
        request.value = value;
        request.expire = expire;
        
        RpcFullText.Response.DeleteByValue response = rpc.deleteByValue(request);
        return response.status.getValue();
    }
    
    public int reindex(String key) throws RpcException {
        RpcFullText.Request.Reindex request = RpcFullText.Request.Reindex.newInstance();
        request.key = key;
        
        RpcFullText.Response.Reindex response = rpc.reindex(request);
        return response.status.getValue();
    }
    
}
