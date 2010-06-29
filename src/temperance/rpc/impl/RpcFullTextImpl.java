package temperance.rpc.impl;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chasen.mecab.wrapper.Tagger;

import temperance.core.Configure;
import temperance.core.FullTextCommand;
import temperance.core.Pooling;
import temperance.exception.RpcException;
import temperance.hash.Hash;
import temperance.hash.HashFunction;
import temperance.hashing.CSVHashing;
import temperance.hashing.GramHashing;
import temperance.hashing.Hashing;
import temperance.hashing.MecabHashing;
import temperance.hashing.MecabNodeFilter;
import temperance.hashing.PrefixHashing;
import temperance.hashing.SSVHashing;
import temperance.hashing.TSVHashing;
import temperance.rpc.RpcFullText;
import temperance.rpc.RpcFullText.Request.Parser;

public class RpcFullTextImpl implements RpcFullText {
    
    protected static final Log logger = LogFactory.getLog(RpcFullTextImpl.class);

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
        switch(parser){
        case BIGRAM:
            return new GramHashing(hashFunction);
        case PREFIX:
            return new PrefixHashing(hashFunction);
        case MECAB:
            return new MecabHashing(hashFunction, tagger, nodeFilter);
        case HASH_CSV:
            return new CSVHashing(hashFunction);
        case HASH_TSV:
            return new TSVHashing(hashFunction);
        case HASH_SSV:
            return new SSVHashing(hashFunction);
        }
        throw new RuntimeException("unknown parser: " + parser);
    }
    
    public Response.Search search(Request.Search request) throws RpcException {
        final String key = request.key;
        final String str = request.str;
        final Parser parser = request.parser;
        
        if(logger.isDebugEnabled()){
            logger.debug(new StringBuilder("search (")
                .append("key=").append(key).append(",")
                .append("str=").append(str).append(",")
                .append("parser=").append(parser)
                .append(")")
            );
        }
        
        final FullTextCommand command = new FullTextCommand(pooling);
        try {
            Hashing hashing = createHashing(parser);
            List<Hash> hashes = hashing.parse(str);
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
        final boolean async = request.asyncRequest;
        
        if(logger.isDebugEnabled()){
            logger.debug(new StringBuilder("add (")
                .append("key=").append(key).append(",")
                .append("str=").append(str).append(",")
                .append("value=").append(value).append(",")
                .append("expire=").append(expire).append(",")
                .append("parser=").append(parser).append(",")
                .append("async=").append(async)
                .append(")")
            );
        }
        
        final Hashing hashing = createHashing(parser);
        final List<Hash> hashes = hashing.parse(str);
        
        final FullTextCommand command = new FullTextCommand(pooling);
        final List<Future<Long>> futures = command.addAll(key, hashes, value, expire);
        if(async){
            Response.Add response = Response.Add.newInstance();
            response.status = Response.Status.ENQUEUE;
            return response;
        }
        
        //
        // sync request
        //
        Response.Add response = Response.Add.newInstance();
        try {
            for(Future<Long> future: futures){
                future.get();
            }
            response.status = Response.Status.SUCCESS;
        } catch(InterruptedException e){
            if(logger.isInfoEnabled()){
                logger.info(e.getMessage(), e);
            }
            
            response.status = Response.Status.FAILURE;
        } catch(ExecutionException e){
            if(logger.isInfoEnabled()){
                logger.info(e.getMessage(), e);
            }

            response.status = Response.Status.FAILURE;
        }
        return response;
    }
    
    public Response.Delete delete(Request.Delete request) throws RpcException {
        final String key = request.key;
        final int expire = request.expire;
        final boolean async = request.asyncRequest;
        
        if(logger.isDebugEnabled()){
            logger.debug(new StringBuilder("delete (")
                .append("key=").append(key).append(",")
                .append("expire=").append(expire).append(",")
                .append("asyc=").append(async)
                .append(")")
            );
        }

        final FullTextCommand command = new FullTextCommand(pooling);
        try {
            Future<Boolean> future = command.deleteAll(key, expire);
            
            if(async){
                Response.Delete response = Response.Delete.newInstance();
                response.status = Response.Status.ENQUEUE;
                return response;
            }
            
            //
            // sync request
            //
            Boolean success = future.get();
            Response.Delete response = Response.Delete.newInstance();
            if(success.booleanValue()){
                response.status = Response.Status.SUCCESS;
            } else {
                response.status = Response.Status.FAILURE;
            }
            return response;
        } catch (ExecutionException e) {
            throw new RpcException(e);
        } catch (InterruptedException e) {
            throw new RpcException(e);
        }
    }
    
    public Response.DeleteByValue deleteByValue(Request.DeleteByValue request) throws RpcException {
        final String key = request.key;
        final String value = request.value;
        final int expire = request.expire;
        final boolean async = request.asyncRequest;
        
        if(logger.isDebugEnabled()){
            logger.debug(new StringBuilder("deleteByValue (")
                .append("key=").append(key).append(",")
                .append("value=").append(value).append(",")
                .append("expire=").append(expire).append(",")
                .append("async=").append(async)
                .append(")")
            );
        }

        final FullTextCommand command = new FullTextCommand(pooling);
        try {
            Future<Boolean> future = command.deleteAllValues(key, expire, value);
            
            if(async){
                Response.DeleteByValue response = Response.DeleteByValue.newInstance();
                response.status = Response.Status.ENQUEUE;
                return response;
            }
            
            //
            // sync request
            //
            Boolean success = future.get();
            Response.DeleteByValue response = Response.DeleteByValue.newInstance();
            if(success.booleanValue()){
                response.status = Response.Status.SUCCESS;
            } else {
                response.status = Response.Status.FAILURE;
            }
            return response;
        } catch (ExecutionException e) {
            throw new RpcException(e);
        } catch (InterruptedException e) {
            throw new RpcException(e);
        }
    }
    
    public Response.Reindex reindex(Request.Reindex request) throws RpcException {
        final String key = request.key;
        final boolean async = request.asyncRequest;
        
        if(logger.isDebugEnabled()){
            logger.debug(new StringBuilder("reindex (")
                .append("key=").append(key).append(",")
                .append("async=").append(async)
                .append(")")
            );
        }
        
        final FullTextCommand command = new FullTextCommand(pooling);
        try {
            Future<Boolean> future = command.reindex(key);
            if(async){
                Response.Reindex response = Response.Reindex.newInstance();
                response.status = Response.Status.ENQUEUE;
                return response;
            }
            
            //
            // sync request
            //
            Boolean success = future.get();
            Response.Reindex response = Response.Reindex.newInstance();
            if(success.booleanValue()){
                response.status = Response.Status.SUCCESS;
            } else {
                response.status = Response.Status.FAILURE;
            }
            return response;
        } catch(ExecutionException e){
            throw new RpcException(e);
        } catch(InterruptedException e){
            throw new RpcException(e);
        }
    }
    
}
