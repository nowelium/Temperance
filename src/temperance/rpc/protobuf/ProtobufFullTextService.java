package temperance.rpc.protobuf;

import temperance.exception.RpcException;
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
        case HASH_CSV:
            return RpcFullText.Request.Parser.HASH_CSV;
        case HASH_TSV:
            return RpcFullText.Request.Parser.HASH_TSV;
        case HASH_SSV:
            return RpcFullText.Request.Parser.HASH_SSV;
        }
        throw new RuntimeException("unkown parser:" + parser);
    }
    
    protected static Response.Status convert(RpcFullText.Response.Status status){
        switch(status){
        case SUCCESS:
            return Response.Status.SUCCESS;
        case ENQUEUE:
            return Response.Status.ENQUEUE;
        case FAILURE:
            return Response.Status.FAILURE;
        case TIMEOUT:
            return Response.Status.TIMEOUT;
        }
        throw new RuntimeException("unknown status:" + status);
    }

    public Response.Search search(RpcController controller, Request.Search get) throws ServiceException {
        RpcFullText.Request.Search request = RpcFullText.Request.Search.newInstance();
        request.key = get.getKey();
        request.str = get.getStr();
        request.parser = convert(get.getParser());
        
        try {
            RpcFullText.Response.Search response = rpc.search(request);
            
            Response.Search.Builder builder = Response.Search.newBuilder();
            builder.addAllValues(response.values);
            return builder.build();
        } catch(RpcException e){
            throw new ServiceException(e.getMessage());
        }
    }

    public Response.Add add(RpcController controller, Request.Add set) throws ServiceException {
        RpcFullText.Request.Add request = RpcFullText.Request.Add.newInstance();
        request.key = set.getKey();
        request.str = set.getStr();
        request.value = set.getValue();
        request.expire = set.getExpire();
        request.parser = convert(set.getParser());
        
        try {
            RpcFullText.Response.Add response = rpc.add(request);
            
            Response.Add.Builder builder = Response.Add.newBuilder();
            builder.setStatus(convert(response.status));
            return builder.build();
        } catch(RpcException e){
            throw new ServiceException(e.getMessage());
        }
    }
    
    public Response.Delete delete(RpcController controller, Request.Delete delete) throws ServiceException {
        RpcFullText.Request.Delete request = RpcFullText.Request.Delete.newInstance();
        request.key = delete.getKey();
        request.expire = delete.getExpire();
        
        try {
            RpcFullText.Response.Delete response = rpc.delete(request);
            
            Response.Delete.Builder builder = Response.Delete.newBuilder();
            builder.setStatus(convert(response.status));
            return builder.build();
        } catch(RpcException e){
            throw new ServiceException(e.getMessage());
        }
    }
    
    public Response.DeleteByValue deleteByValue(RpcController controller, Request.DeleteByValue deleteByValue) throws ServiceException {
        RpcFullText.Request.DeleteByValue request = RpcFullText.Request.DeleteByValue.newInstance();
        request.key = deleteByValue.getKey();
        request.value = deleteByValue.getValue();
        request.expire = deleteByValue.getExpire();
        
        try {
            RpcFullText.Response.DeleteByValue response = rpc.deleteByValue(request);
            
            Response.DeleteByValue.Builder builder = Response.DeleteByValue.newBuilder();
            builder.setStatus(convert(response.status));
            return builder.build();
        } catch(RpcException e){
            throw new ServiceException(e.getMessage());
        }
    }
    
    public Response.Reindex reindex(RpcController controller, Request.Reindex reindex) throws ServiceException {
        RpcFullText.Request.Reindex request = RpcFullText.Request.Reindex.newInstance();
        request.key = reindex.getKey();
        
        try {
            RpcFullText.Response.Reindex response = rpc.reindex(request);
            
            Response.Reindex.Builder builder = Response.Reindex.newBuilder();
            builder.setStatus(convert(response.status));
            return builder.build();
        } catch(RpcException e){
            throw new ServiceException(e.getMessage());
        }
    }

}
