package temperance.handler.function;

import java.util.List;

import libmemcached.exception.LibMemcachedException;
import temperance.handler.function.exception.ExecutionException;
import temperance.ql.InternalFunction;
import temperance.storage.MemcachedList;
import temperance.util.Lists;

public class DataFunction implements InternalFunction {
    
    protected static final int SPLIT = 1000;
    
    protected final FunctionContext context;
    
    public DataFunction(FunctionContext context){
        this.context = context;
    }

    public List<String> deleteIn(String fromKey, List<String> args) throws ExecutionException {
        return null;
    }

    public List<String> deleteNot(String fromKey, List<String> args) throws ExecutionException {
        return null;
    }

    public List<String> selectIn(String fromKey, List<String> args) throws ExecutionException {
        final String targetKey = args.get(0);
        MemcachedList list = new MemcachedList(context.getPool());
        try {
            List<String> fromValues = Lists.newArrayList();
            long fromCount = list.count(fromKey);
            for(long i = 0; i < fromCount; i += SPLIT){
                fromValues.addAll(list.get(fromKey, i, SPLIT));
            }
            
            List<String> targetValues = Lists.newArrayList();
            long targetCount = list.count(targetKey);
            for(long i = 0; i < targetCount; i += SPLIT){
                targetValues.addAll(list.get(targetKey, i, SPLIT));
            }
            
            // narrow
            // fromValues contains all(only) tagetValues
            fromValues.retainAll(targetValues);
            return fromValues;
        } catch(LibMemcachedException e){
            throw new ExecutionException(e);
        }
    }

    public List<String> selectNot(String fromKey, List<String> args) throws ExecutionException {
        final String targetKey = args.get(0);
        MemcachedList list = new MemcachedList(context.getPool());
        try {
            List<String> fromValues = Lists.newArrayList();
            long fromCount = list.count(fromKey);
            for(long i = 0; i < fromCount; i += SPLIT){
                fromValues.addAll(list.get(fromKey, i, SPLIT));
            }
            
            // narrow
            long targetCount = list.count(targetKey);
            for(long i = 0; i < targetCount; i += SPLIT){
                List<String> results = list.get(targetKey, i, SPLIT);
                for(String result: results){
                    // contains remove
                    fromValues.remove(result);
                }
            }
            
            return fromValues;
        } catch(LibMemcachedException e){
            throw new ExecutionException(e);
        }
    }

}
