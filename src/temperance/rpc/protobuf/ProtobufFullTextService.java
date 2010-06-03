package temperance.rpc.protobuf;

import temperance.protobuf.FullText.FullTextService;
import temperance.protobuf.FullText.Request;
import temperance.protobuf.FullText.Response;
import temperance.rpc.RpcFullText;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

public class ProtobufFullTextService implements FullTextService.BlockingInterface {
    
    protected final RpcFullText rpc;
    
    public ProtobufFullTextService(RpcFullText rpc){
        this.rpc = rpc;
    }
    
    protected static RpcFullText.Request.Parser convert(Request.Parser parser){
        switch(parser){
        case MECAB:
            return RpcFullText.Request.Parser.MECAB;
        case BIGRAM:
            return RpcFullText.Request.Parser.BIGRAM;
        case PREFIX:
            return RpcFullText.Request.Parser.PREFIX;
        }
        throw new RuntimeException("unkown parser:" + parser);
    }

    public Response.Get search(RpcController controller, Request.Get get) throws ServiceException {
        RpcFullText.Request.Search request = new RpcFullText.Request.Search();
        request.key = get.getKey();
        request.str = get.getStr();
        request.parser = convert(get.getParser());
        
        RpcFullText.Response.Search response = rpc.search(request);
        
        Response.Get.Builder builder = Response.Get.newBuilder();
        builder.addAllValues(response.values);
        return builder.build();
    }

    public Response.Set set(RpcController controller, Request.Set set) throws ServiceException {
        RpcFullText.Request.Set request = new RpcFullText.Request.Set();
        request.key = set.getKey();
        request.str = set.getStr();
        request.value = set.getValue();
        request.expire = set.getExpire();
        request.parser = convert(set.getParser());
        
        RpcFullText.Response.Set response = rpc.set(request);
        
        Response.Set.Builder builder = Response.Set.newBuilder();
        builder.setSucceed(response.succeed);
        return builder.build();
    }

}
