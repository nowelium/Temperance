package temperance.rpc.impl;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import libmemcached.exception.LibMemcachedException;

import org.chasen.mecab.wrapper.Tagger;

import temperance.core.Configure;
import temperance.core.FullTextCommand;
import temperance.core.Pooling;
import temperance.exception.RpcException;
import temperance.ft.GramHashing;
import temperance.ft.Hashing;
import temperance.ft.MecabHashing;
import temperance.ft.MecabNodeFilter;
import temperance.ft.PrefixHashing;
import temperance.function.AbstractTaggerFunction;
import temperance.function.FunctionContext;
import temperance.hash.HashFunction;
import temperance.rpc.RpcFullText;
import temperance.rpc.RpcFullText.Request.Parser;
import temperance.storage.MemcachedFullText;

public class RpcFullTextImpl implements RpcFullText {

    protected final Configure configure;
    
    protected final HashFunction hashFunction;
    
    protected final MecabNodeFilter nodeFilter;

    protected final Tagger tagger;
    
    protected final Pooling pooling;
    
    public RpcFullTextImpl(Configure configure, Pooling pooling){
        this.configure = configure;
        this.hashFunction = configure.getFullTextHashFunction();
        this.nodeFilter = configure.getNodeFilter();
        this.tagger = Tagger.create("-r", configure.getMecabrc());
        this.pooling = pooling;
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
        
        final FullTextCommand command = new FullTextCommand(pooling);
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

    public Response.Add add(Request.Add request)  throws RpcException {
        final String key = request.key;
        final String str = request.str;
        final String value = request.value;
        final int expire = request.expire;
        final Parser parser = request.parser;
        
        try {
            final MemcachedFullText fulltext = new MemcachedFullText(pooling.getConnectionPool());
            final Hashing hashing = createHashing(parser);
            final List<Long> hashes = hashing.parse(str);
            fulltext.addAll(key, hashes, value, expire);
            
            Response.Add response = Response.Add.newInstance();
            response.succeed = true;
            return response;
        } catch (LibMemcachedException e) {
            throw new RpcException(e);
        }
    }
    
    protected static class TaggerFunction extends AbstractTaggerFunction {
        protected final Hashing hashing;
        protected TaggerFunction(FunctionContext context, Hashing hashing){
            super(context);
            this.hashing = hashing;
        }
        protected Hashing createHashing(List<String> args){
            return hashing;
        }
    }

}
