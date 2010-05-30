package temperance.handler;

import java.util.ArrayList;
import java.util.List;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.MemcachedServerList;

import org.chasen.mecab.wrapper.Tagger;

import temperance.hash.Hash;
import temperance.hash.HashFunction;
import temperance.protobuf.FullText.FullTextService;
import temperance.protobuf.FullText.Request;
import temperance.protobuf.FullText.Response;
import temperance.protobuf.FullText.Request.Parser;
import temperance.storage.MemcachedFullTextList;
import temperance.util.FullTextUtil;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

public class FullTextServiceHandler implements FullTextService.BlockingInterface {
    
    protected static final int SPLIT = 3000;
    
    protected final Context context;
    
    protected final HashFunction hashFunction;
    
    protected final Tagger tagger;
    
    public FullTextServiceHandler(Context context){
        this.context = context;
        this.hashFunction = Hash.valueOf(context.getFullTextHashFunction());
        this.tagger = Tagger.create("-r", context.getMecabrc());
    }
    
    protected MemcachedClient createMemcachedClient(){
        MemcachedClient client = new MemcachedClient();
        MemcachedServerList servers = client.getServerList();
        servers.parse(context.getMemcached());
        servers.push();
        return client;
    }
    
    public Response.Set set(RpcController controller, Request.Set request) throws ServiceException {
        String key = request.getKey();
        String str = request.getStr();
        String value = request.getValue();
        Parser parser = request.getParser();
        MemcachedClient client = createMemcachedClient();
        
        MemcachedFullTextList list = new MemcachedFullTextList(client);
        try {
            switch(parser){
                case MECAB: {
                    List<Long> hashes = FullTextUtil.mecab(hashFunction, tagger, str);
                    for(Long hash: hashes){
                        list.add(key, hash, value);
                    }
                    return Response.Set.newBuilder().setSucceed(true).build();
                }
                case BI_GRAM: {
                    List<Long> hashes = FullTextUtil.gram(hashFunction, str);
                    for(Long hash: hashes){
                        list.add(key, hash, value);
                    }
                    return Response.Set.newBuilder().setSucceed(true).build();
                }
            }
            return Response.Set.newBuilder().setSucceed(false).build();
        } catch(LibMemcachedException e){
            e.printStackTrace();
            return Response.Set.newBuilder().setSucceed(false).build();
        }
    }
    
    public Response.Get search(RpcController controller, Request.Get request) throws ServiceException {
        String key = request.getKey();
        String str = request.getStr();
        Parser parser = request.getParser();
        MemcachedClient client = createMemcachedClient();
        
        MemcachedFullTextList list = new MemcachedFullTextList(client);
        
        Response.Get.Builder builder = Response.Get.newBuilder();
        try {
            switch(parser){
                case MECAB:{
                    List<Long> hashes = FullTextUtil.mecab(hashFunction, tagger, str);
                    for(Long hash: hashes){
                        builder.addAllValues(getAll(list, key, hash));
                    }
                    return builder.build();
                }
                case BI_GRAM:{
                    List<Long> hashes = FullTextUtil.gram(hashFunction, str, 2);
                    for(Long hash: hashes){
                        builder.addAllValues(getAll(list, key, hash));
                    }
                    return builder.build();
                }
            }
            return builder.build();
        } catch(LibMemcachedException e){
            throw new ServiceException(e.getMessage());
        }
    }
    
    protected List<String> getAll(MemcachedFullTextList list, String key, Long hash) throws LibMemcachedException {
        List<String> results = new ArrayList<String>();
        long count = list.count(key, hash);
        for(long i = 0; i < count; i += SPLIT){
            results.addAll(list.get(key, hash, i, SPLIT));
        }
        return results;
    }

}
