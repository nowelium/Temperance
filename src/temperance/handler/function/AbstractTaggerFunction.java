package temperance.handler.function;

import java.util.List;

import libmemcached.exception.LibMemcachedException;
import temperance.ft.Hashing;
import temperance.handler.function.exception.ExecutionException;
import temperance.ql.InternalFunction;
import temperance.storage.MemcachedFullText;
import temperance.util.Lists;
import temperance.util.Lists.IntersectList;

public abstract class AbstractTaggerFunction implements InternalFunction {
    
    protected static final int SPLIT = 3000;

    protected final FunctionContext context;
    
    protected final MemcachedFullText ft;
    
    public AbstractTaggerFunction(FunctionContext context){
        this.context = context;
        this.ft = new MemcachedFullText(context.getPool().get());
    }
    
    protected abstract Hashing createHashing(List<String> args);
    
    public List<String> deleteIn(String key, List<String> args) throws ExecutionException {
        throw new ExecutionException("not yet implemented");
    }

    public List<String> deleteNot(String key, List<String> args) throws ExecutionException {
        throw new ExecutionException("not yet implemented");
    }

    public List<String> selectIn(String key, List<String> args) throws ExecutionException {
        if(args.isEmpty()){
            throw new ExecutionException("arguments was empty");
        }
        
        final String str = args.get(0);
        final Hashing hashing = createHashing(args);
        try {
            IntersectList<String> returnValue = Lists.newIntersectList();
            List<Long> allHashes = hashing.parse(str);
            for(Long hash: allHashes){
                List<String> results = getAll(key, hash);
                returnValue.intersect(results);
            }
            
            return returnValue;
        } catch(LibMemcachedException e){
            throw new ExecutionException(e);
        }
    }

    public List<String> selectNot(String key, List<String> args) throws ExecutionException {
        if(args.isEmpty()){
            throw new ExecutionException("arguments was empty");
        }
        
        final String str = args.get(0);
        final Hashing hashing = createHashing(args);
        try {
            List<Long> ignoreHashes = hashing.parse(str);
            List<Long> allKeys = getAll(key);
            // remove ignore keys
            allKeys.removeAll(ignoreHashes);
            
            List<String> returnValue = Lists.newArrayList();
            for(Long selectHash: allKeys){
                returnValue.addAll(getAll(key, selectHash));
            }
            return returnValue;
        } catch(LibMemcachedException e){
            throw new ExecutionException(e);
        }
    }
    
    protected List<Long> getAll(String key) throws LibMemcachedException {
        final List<Long> results = Lists.newArrayList();
        
        long allHasheCount = ft.count(key);
        for(long i = 0; i < allHasheCount; i += SPLIT){
            results.addAll(ft.get(key, i, SPLIT));
        }
        return results;
    }
    
    protected List<String> getAll(String key, Long hash) throws LibMemcachedException {
        final List<String> results = Lists.newArrayList();
        
        long count = ft.count(key, hash);
        for(long i = 0; i < count; i += SPLIT){
            long limit = SPLIT;
            if(count < SPLIT){
                limit = count;
            }
            results.addAll(ft.get(key, hash, i, limit));
        }
        return results;
    }
    
}
