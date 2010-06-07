package temperance.rpc.impl;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import libmemcached.exception.LibMemcachedException;

import org.chasen.mecab.wrapper.Tagger;

import temperance.exception.RpcException;
import temperance.ft.GramHashing;
import temperance.ft.Hashing;
import temperance.ft.MecabHashing;
import temperance.ft.MecabNodeFilter;
import temperance.ft.PrefixHashing;
import temperance.hash.HashFunction;
import temperance.memcached.FullTextCommand;
import temperance.memcached.ConnectionPool;
import temperance.rpc.Context;
import temperance.rpc.RpcFullText;
import temperance.rpc.RpcFullText.Request.Parser;
import temperance.storage.MemcachedFullText;

public class RpcFullTextImpl implements RpcFullText {

    protected final Context context;
    
    protected final HashFunction hashFunction;
    
    protected final MecabNodeFilter nodeFilter;

    protected final Tagger tagger;
    
    protected final ConnectionPool pool;
    
    protected final MemcachedFullText fulltext;
    
    public RpcFullTextImpl(Context context, ConnectionPool pool){
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
    
    public Response.Search search(Request.Search request)  throws RpcException {
        final String key = request.key;
        final String str = request.str;
        final Parser parser = request.parser;
        
        final FullTextCommand command = new FullTextCommand(pool);
        try {
            Hashing hashing = createHashing(parser);
            List<Long> hashes = hashing.parse(str);
            List<Future<List<String>>> futures = command.getAll(key, hashes);
            
            Response.Search response = Response.Search.newInstance();
            for(Future<List<String>> future: futures){
                List<String> result = future.get();
                response.values.addAll(result);
            }
            return response;
        } catch (ExecutionException e) {
            throw new RpcException(e);
        } catch (InterruptedException e) {
            throw new RpcException(e);
        }
    }

    public Response.Set set(Request.Set request)  throws RpcException {
        final String key = request.key;
        final String str = request.str;
        final String value = request.value;
        final int expire = request.expire;
        final Parser parser = request.parser;
        
        try {
            Hashing hashing = createHashing(parser);
            List<Long> hashes = hashing.parse(str);
            for(Long hash: hashes){
                fulltext.add(key, hash, value, expire);
            }
            Response.Set response = Response.Set.newInstance();
            response.succeed = true;
            return response;
        } catch(LibMemcachedException e){
            throw new RpcException(e);
        }
    }

}
