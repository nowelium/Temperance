package temperance.handler;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import libmemcached.exception.LibMemcachedException;

import org.chasen.mecab.wrapper.Tagger;

import temperance.ft.GramHashing;
import temperance.ft.Hashing;
import temperance.ft.MecabHashing;
import temperance.ft.MecabNodeFilter;
import temperance.ft.PrefixHashing;
import temperance.hash.HashFunction;
import temperance.memcached.FullTextCommand;
import temperance.memcached.Pool;
import temperance.protobuf.FullText.FullTextService;
import temperance.protobuf.FullText.Request;
import temperance.protobuf.FullText.Response;
import temperance.protobuf.FullText.Request.Parser;
import temperance.storage.MemcachedFullText;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

public class FullTextServiceHandler implements FullTextService.BlockingInterface {
    
    protected final Context context;
    
    protected final HashFunction hashFunction;
    
    protected final MecabNodeFilter nodeFilter;

    protected final Tagger tagger;
    
    protected final Pool pool;
    
    protected final MemcachedFullText fulltext;
    
    public FullTextServiceHandler(Context context, Pool pool){
        this.context = context;
        this.hashFunction = context.getFullTextHashFunction();
        this.nodeFilter = context.getNodeFilter();
        this.tagger = Tagger.create("-r", context.getMecabrc());
        this.pool = pool;
        this.fulltext = new MemcachedFullText(pool);
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
        
        try {
            Hashing hashing = createHashing(parser);
            List<Long> hashes = hashing.parse(str);
            for(Long hash: hashes){
                fulltext.add(key, hash, value, expire);
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
        
        final FullTextCommand command = new FullTextCommand(pool);
        Response.Get.Builder builder = Response.Get.newBuilder();
        try {
            Hashing hashing = createHashing(parser);
            List<Long> hashes = hashing.parse(str);
            List<Future<List<String>>> futures = command.getAll(key, hashes);
            for(Future<List<String>> future: futures){
                builder.addAllValues(future.get());
            }
            return builder.build();
        } catch (InterruptedException e) {
            throw new ServiceException(e.getMessage());
        } catch (ExecutionException e) {
            throw new ServiceException(e.getMessage());
        }
    }
    
}
