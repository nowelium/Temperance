package temperance.handler.function;

import java.util.ArrayList;
import java.util.List;

import libmemcached.exception.LibMemcachedException;
import temperance.ft.Hashing;
import temperance.handler.function.exception.ExecutionException;
import temperance.ql.InternalFunction;
import temperance.storage.MemcachedFullText;
import temperance.util.ListUtils;

public abstract class AbstractTaggerFunction implements InternalFunction {
    
    protected static final int SPLIT = 3000;

    protected final FunctionContext context;
    
    protected AbstractTaggerFunction(FunctionContext context){
        this.context = context;
    }
    
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
        final MemcachedFullText list = new MemcachedFullText(context.getClient());
        try {
            List<String> returnValue = new ArrayList<String>();
            List<Long> hashes = hashing.parse(str);
            for(Long hash: hashes){
                long count = list.count(key, hash);
                for(long i = 0; i < count; i += SPLIT){
                    returnValue.addAll(list.get(key, hash, i, SPLIT));
                }
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
        final MemcachedFullText list = new MemcachedFullText(context.getClient());
        try {
            List<Long> ignoreHashes = hashing.parse(str);
            List<String> selectHashes = new ArrayList<String>();
            long allHashes = list.count(key);
            
            // FIXME: exclude not in key
            for(long i = 0; i < allHashes; i += SPLIT){
                List<String> storedHashes = list.get(key, i, SPLIT);
                for(Long ignore: ignoreHashes){
                    storedHashes.remove(ignore.toString());
                }
                selectHashes.addAll(storedHashes);
            }
            selectHashes = ListUtils.unique(selectHashes);
            
            List<String> returnValue = new ArrayList<String>();
            for(String selectHash: selectHashes){
                Long hash = Long.valueOf(selectHash);
                long count = list.count(key, hash);
                for(long i = 0; i < count; i += SPLIT){
                    returnValue.addAll(list.get(key, hash, i, SPLIT));
                }
            }
            return returnValue;
        } catch(LibMemcachedException e){
            throw new ExecutionException(e);
        }
    }
    
    protected abstract Hashing createHashing(List<String> args);
    
}
