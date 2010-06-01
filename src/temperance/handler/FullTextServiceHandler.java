package temperance.handler;

import java.util.ArrayList;
import java.util.List;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.MemcachedServerList;

import org.chasen.mecab.wrapper.Tagger;

import temperance.ft.GramHashing;
import temperance.ft.Hashing;
import temperance.ft.MecabHashing;
import temperance.ft.MecabNodeFilter;
import temperance.ft.PrefixHashing;
import temperance.hash.HashFunction;
import temperance.protobuf.FullText.FullTextService;
import temperance.protobuf.FullText.Request;
import temperance.protobuf.FullText.Response;
import temperance.protobuf.FullText.Request.Parser;
import temperance.storage.MemcachedFullText;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

public class FullTextServiceHandler implements FullTextService.BlockingInterface {
    
    protected static final int SPLIT = 3000;
    
    protected final Context context;
    
    protected final HashFunction hashFunction;
    
    protected final MecabNodeFilter nodeFilter;

    protected final Tagger tagger;
    
    public FullTextServiceHandler(Context context){
        this.context = context;
        this.hashFunction = context.getFullTextHashFunction();
        this.nodeFilter = context.getNodeFilter();
        this.tagger = Tagger.create("-r", context.getMecabrc());
    }
    
    protected MemcachedClient createMemcachedClient(){
        MemcachedClient client = new MemcachedClient();
        MemcachedServerList servers = client.getServerList();
        servers.parse(context.getMemcached());
        servers.push();
        return client;
    }
    
    protected Hashing createHashing(Parser parser){
        if(Parser.BIGRAM.equals(parser)){
            return new GramHashing(hashFunction);
        }
        if(Parser.PREFIX.equals(parser)){
            return new PrefixHashing(hashFunction);
        }
        return new MecabHashing(hashFunction, tagger, nodeFilter);
    }
    
    public Response.Set set(RpcController controller, Request.Set request) throws ServiceException {
        final String key = request.getKey();
        final String str = request.getStr();
        final String value = request.getValue();
        final int expire = request.getExpire();
        final Parser parser = request.getParser();
        MemcachedClient client = createMemcachedClient();
        
        MemcachedFullText list = new MemcachedFullText(client);
        try {
            Hashing hashing = createHashing(parser);
            List<Long> hashes = hashing.parse(str);
            for(Long hash: hashes){
                list.add(key, hash, value, expire);
            }
            return Response.Set.newBuilder().setSucceed(true).build();
        } catch(LibMemcachedException e){
            e.printStackTrace();
            return Response.Set.newBuilder().setSucceed(false).build();
        }
    }
    
    public Response.Get search(RpcController controller, Request.Get request) throws ServiceException {
        final String key = request.getKey();
        final String str = request.getStr();
        final Parser parser = request.getParser();
        MemcachedClient client = createMemcachedClient();
        
        MemcachedFullText list = new MemcachedFullText(client);
        
        Response.Get.Builder builder = Response.Get.newBuilder();
        try {
            Hashing hashing = createHashing(parser);
            List<Long> hashes = hashing.parse(str);
            for(Long hash: hashes){
                builder.addAllValues(getAll(list, key, hash));
            }
            return builder.build();
        } catch(LibMemcachedException e){
            throw new ServiceException(e.getMessage());
        }
    }
    
    protected List<String> getAll(MemcachedFullText list, String key, Long hash) throws LibMemcachedException {
        List<String> results = new ArrayList<String>();
        long count = list.count(key, hash);
        for(long i = 0; i < count; i += SPLIT){
            results.addAll(list.get(key, hash, i, SPLIT));
        }
        return results;
    }

}
