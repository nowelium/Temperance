package temperance.function;

import java.util.List;

import libmemcached.exception.LibMemcachedException;
import temperance.exception.ExecutionException;
import temperance.ql.InternalFunction;
import temperance.storage.MemcachedList;
import temperance.util.Lists;

public class ValueFunction implements InternalFunction {
    
    protected static final int SPLIT = 1000;
    
    protected final FunctionContext context;
    
    public ValueFunction(FunctionContext context){
        this.context = context;
    }
    
    public List<String> deleteIn(String key, List<String> args) throws ExecutionException {
        throw new ExecutionException("not yet implemented");
    }

    public List<String> deleteNot(String key, List<String> args) throws ExecutionException {
        throw new ExecutionException("not yet implemented");
    }

    /**
     * [1, 2, 3, 4, 5] in arg(1, 2) => results(1, 2)
     */
    public List<String> selectIn(String key, List<String> args) throws ExecutionException {
        try {
            MemcachedList list = new MemcachedList(context.getPool());
            List<String> returnValue = Lists.newArrayList();
            
            Condition condition = new NotContainsReject(args);
            long count = list.count(key);
            for(long i = 0; i < count; i += SPLIT){
                long limit = SPLIT;
                if(count < SPLIT){
                    limit = count;
                }
                
                List<String> results = list.get(key, i, limit);
                for(String result: results){
                    // continue when not contains
                    if(condition.reject(result)){
                        continue;
                    }
                    returnValue.add(result);
                }
            }
            return returnValue;
        } catch(LibMemcachedException e){
            throw new ExecutionException(e);
        }
    }

    /**
     * [1, 2, 3, 4, 5] not arg(1, 2) => results(3, 4, 5)
     */
    public List<String> selectNot(String key, List<String> args) throws ExecutionException {
        try {
            MemcachedList list = new MemcachedList(context.getPool());
            List<String> returnValue = Lists.newArrayList();
            
            Condition condition = new ContainsReject(args);
            long count = list.count(key);
            for(long i = 0; i < count; i += SPLIT){
                long limit = SPLIT;
                if(count < SPLIT){
                    limit = count;
                }
                
                List<String> results = list.get(key, i, limit);
                for(String result: results){
                    // continue when contains
                    if(condition.reject(result)){
                        continue;
                    }
                    returnValue.add(result);
                }
            }
            return returnValue;
        } catch(LibMemcachedException e){
            throw new ExecutionException(e);
        }
    }

    protected static interface Condition {
        public boolean reject(String value);
    }
    
    protected static class ContainsReject implements Condition {
        private final List<String> args;
        private ContainsReject(List<String> args){
            this.args = args;
        }
        public boolean reject(String value){
            return args.contains(value);
        }
    }
    
    protected static class NotContainsReject extends ContainsReject {
        private NotContainsReject(List<String> args){
            super(args);
        }
        @Override
        public boolean reject(String value){
            return !super.reject(value);
        }
    }

}
