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
import temperance.hash.HashFunction;
import temperance.hashing.GramHashing;
import temperance.hashing.Hashing;
import temperance.hashing.MecabHashing;
import temperance.hashing.MecabNodeFilter;
import temperance.hashing.PrefixHashing;
import temperance.rpc.RpcFullText;
import temperance.rpc.RpcFullText.Request.Parser;
import temperance.storage.impl.MemcachedFullText;

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
    
    public Response.Search search(Request.Search request) throws RpcException {
        final String key = request.key;
        final String str = request.str;
        final Parser parser = request.parser;
        
        final FullTextCommand command = new FullTextCommand(pooling);
        try {
            Hashing hashing = createHashing(parser);
            List<Long> hashes = hashing.parse(str);
            List<Future<List<String>>> futures = command.getValues(key, hashes);
            
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
    
    public Response.Add add(Request.Add request) throws RpcException {
        final String key = request.key;
        final String str = request.str;
        final String value = request.value;
        final int expire = request.expire;
        final Parser parser = request.parser;
        // TODO: sync option
        final boolean sync = false;
        
        try {
            final Hashing hashing = createHashing(parser);
            final List<Long> hashes = hashing.parse(str);
            
            final FullTextCommand command = new FullTextCommand(pooling);
            final List<Future<Long>> futures = command.addAll(key, hashes, value, expire);
            if(sync){
                Response.Add response = Response.Add.newInstance();
                try {
                    for(Future<Long> future: futures){
                        future.get();
                    }
                    response.status = Response.Status.SUCCESS;
                } catch(InterruptedException e){
                    e.printStackTrace();
                    response.status = Response.Status.FAILURE;
                } catch(ExecutionException e){
                    response.status = Response.Status.FAILURE;
                }
                return response;
            }
            
            Response.Add response = Response.Add.newInstance();
            response.status = Response.Status.ENQUEUE;
            return response;
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }
    
    public Response.Delete delete(Request.Delete request) throws RpcException {
        final String key = request.key;
        final int expire = request.expire;
        // TODO: sync option
        final boolean sync = false;
        
        //
        // T.B.D: delete
        //
        
        
        Response.Delete response = Response.Delete.newInstance();
        response.status = Response.Status.SUCCESS;
        return response;
    }
    
    public Response.DeleteByValue deleteByValue(Request.DeleteByValue request) throws RpcException {
        final String key = request.key;
        final String value = request.value;
        final int expire = request.expire;
        // TODO: sync option
        final boolean sync = false;

        //
        // T.B.D: deleteByValue
        //
        
        Response.DeleteByValue response = Response.DeleteByValue.newInstance();
        response.status = Response.Status.SUCCESS;
        return response;
    }
    
}
