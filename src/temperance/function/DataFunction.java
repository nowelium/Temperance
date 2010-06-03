package temperance.function;

import java.util.List;

import libmemcached.exception.LibMemcachedException;
import temperance.exception.ExecutionException;
import temperance.storage.MemcachedList;
import temperance.util.Lists;

public class DataFunction implements InternalFunction {
    
    protected static final int SPLIT = 1000;
    
    protected final FunctionContext context;
    
    public DataFunction(FunctionContext context){
        this.context = context;
    }
    
    public Command createDelete() {
        return new Delete();
    }

    public Command createSelect() {
        return new Select();
    }
    
    protected class Delete implements InternalFunction.Command {
        public List<String> and(String key, List<String> args) throws ExecutionException {
            throw new ExecutionException("not yet implemented");
        }

        public List<String> not(String key, List<String> args) throws ExecutionException {
            throw new ExecutionException("not yet implemented");
        }

        public List<String> or(String key, List<String> args) throws ExecutionException {
            throw new ExecutionException("not yet implemented");
        }
    }
    
    protected class Select implements InternalFunction.Command {
        /**
         * DATA(1, 2, 3, 4, 5) in DATA(2, 3, 5) => results(2, 3, 5)
         */
        public List<String> and(String key, List<String> args) throws ExecutionException {
            final MemcachedList list = new MemcachedList(context.getPool());
            try {
                List<String> fromValues = Lists.newArrayList();
                long fromCount = list.count(key);
                for(long i = 0; i < fromCount; i += SPLIT){
                    fromValues.addAll(list.get(key, i, SPLIT));
                }
                
                for(final String targetKey: args){
                    List<String> targetValues = Lists.newArrayList();
                    long targetCount = list.count(targetKey);
                    for(long i = 0; i < targetCount; i += SPLIT){
                        targetValues.addAll(list.get(targetKey, i, SPLIT));
                    }
                    
                    // narrow
                    // fromValues contains all(only) tagetValues
                    fromValues.retainAll(targetValues);
                }
                return fromValues;
            } catch(LibMemcachedException e){
                throw new ExecutionException(e);
            }
        }

        /**
         * DATA(1, 2, 3, 4, 5) not DATA(2, 3, 5) => results(1, 4)
         */
        public List<String> not(String key, List<String> args) throws ExecutionException {
            final MemcachedList list = new MemcachedList(context.getPool());
            try {
                List<String> fromValues = Lists.newArrayList();
                long fromCount = list.count(key);
                for(long i = 0; i < fromCount; i += SPLIT){
                    fromValues.addAll(list.get(key, i, SPLIT));
                }
                
                for(final String targetKey: args){
                    // narrow
                    long targetCount = list.count(targetKey);
                    for(long i = 0; i < targetCount; i += SPLIT){
                        List<String> results = list.get(targetKey, i, SPLIT);
                        for(String result: results){
                            // contains remove
                            fromValues.remove(result);
                        }
                    }
                }
                
                return fromValues;
            } catch(LibMemcachedException e){
                throw new ExecutionException(e);
            }
        }

        public List<String> or(String key, List<String> args) throws ExecutionException {
            throw new ExecutionException("not yet implemented");
        }
    }

}
