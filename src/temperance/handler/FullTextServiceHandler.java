package temperance.handler;

import java.util.List;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.MemcachedServerList;

import org.chasen.mecab.wrapper.MecabNode;
import org.chasen.mecab.wrapper.Node;
import org.chasen.mecab.wrapper.Path;
import org.chasen.mecab.wrapper.Tagger;

import temperance.hash.Hash;
import temperance.hash.HashFunction;
import temperance.protobuf.FullText.FullTextService;
import temperance.protobuf.FullText.Request;
import temperance.protobuf.FullText.Response;
import temperance.protobuf.FullText.Request.Parser;
import temperance.storage.MemcachedList;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

public class FullTextServiceHandler implements FullTextService.BlockingInterface {
    
    protected final Context context;
    
    protected final HashFunction hashFunction = Hash.MD5;
    
    protected final Tagger tagger = Tagger.create("-r /opt/local/etc/mecabrc");
    
    public FullTextServiceHandler(Context context){
        this.context = context;
    }
    
    protected MemcachedClient createMemcachedClient(){
        MemcachedClient client = new MemcachedClient();
        MemcachedServerList servers = client.getServerList();
        servers.parse(context.getMemcached());
        servers.push();
        return client;
    }

    public Response.Set set(RpcController controller, Request.Set request) throws ServiceException {
        String namespace = request.getNamespace();
        String str = request.getStr();
        String value = request.getValue();
        Parser parser = request.getParser();
        MemcachedClient client = createMemcachedClient();
        
        MemcachedList list = new MemcachedList(client, namespace);
        try {
            switch(parser){
            case MECAB:
                for(MecabNode<Node, Path> node: tagger.iterator(str)){
                    long hash = hashFunction.hash(node.getSurface());
                    list.add(Long.toString(hash), value);
                }
                return Response.Set.newBuilder().setSucceed(true).build();
            case BI_GRAM:
                int length = str.length();
                for(int i = 0; i < length; ++i){
                    int limit = i + 2;
                    if(length < limit){
                        limit = i + 1;
                    }
                    long hash = hashFunction.hash(str.substring(i, limit));
                    list.add(Long.toString(hash), value);
                }
                return Response.Set.newBuilder().setSucceed(true).build();
            }
            return Response.Set.newBuilder().setSucceed(false).build();
        } catch(LibMemcachedException e){
            e.printStackTrace();
            return Response.Set.newBuilder().setSucceed(false).build();
        }
    }
    
    public Response.Get search(RpcController controller, Request.Get request) throws ServiceException {
        String namespace = request.getNamespace();
        String str = request.getStr();
        Parser parser = request.getParser();
        MemcachedClient client = createMemcachedClient();
        
        MemcachedList list = new MemcachedList(client, namespace);
        
        Response.Get.Builder builder = Response.Get.newBuilder();
        try {
            switch(parser){
            case MECAB:
                for(MecabNode<Node, Path> node: tagger.iterator(str)){
                    long hash = hashFunction.hash(node.getSurface());
                    List<String> values = list.get(Long.toString(hash), 0, 3000);
                    builder.addAllValues(values);
                }
                return builder.build();
            case BI_GRAM:
                int length = str.length();
                for(int i = 0; i < length; ++i){
                    int limit = i + 2;
                    if(length < limit){
                        limit = i + 1;
                    }
                    long hash = hashFunction.hash(str.substring(i, limit));
                    List<String> values = list.get(Long.toString(hash), 0, 3000);
                    builder.addAllValues(values);
                }
                return builder.build();
            }
            return builder.build();
        } catch(LibMemcachedException e){
            throw new ServiceException(e.getMessage());
        }
    }

}
