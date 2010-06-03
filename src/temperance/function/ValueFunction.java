package temperance.function;

import java.util.List;

import libmemcached.exception.LibMemcachedException;
import temperance.exception.ExecutionException;
import temperance.storage.MemcachedList;
import temperance.util.Lists;

public class ValueFunction implements InternalFunction {
    
    protected static final int SPLIT = 1000;
    
    protected final FunctionContext context;
    
    public ValueFunction(FunctionContext context){
        this.context = context;
    }
    
    public Command createDelete() {
        return new Delete();
    }

    public Command createSelect() {
        return new Select();
    }
    
    protected List<String> select(final String key, final Condition condition) throws ExecutionException {
        return select(key, new Filter() {
            public List<String> execute(List<String> results){
                List<String> returnValue = Lists.newArrayList();
                for(String value: results){
                    if(condition.reject(value)){
                        continue;
                    }
                    returnValue.add(value);
                }
                return returnValue;
            }
        });
    }
    
    protected List<String> select(final String key, final Filter filter) throws ExecutionException {
        try {
            final MemcachedList list = new MemcachedList(context.getPool());
            List<String> returnValue = Lists.newArrayList();
            
            final long count = list.count(key);
            for(long i = 0; i < count; i += SPLIT){
                long limit = SPLIT;
                if(count < SPLIT){
                    limit = count;
                }
                
                final List<String> results = list.get(key, i, limit);
                returnValue.addAll(filter.execute(results));
            }
            return returnValue;
        } catch(LibMemcachedException e){
            throw new ExecutionException(e);
        }
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
         * [1, 2, 3, 4, 5] in arg(1, 2) => results(1, 2)
         */
        public List<String> and(final String key, final List<String> args) throws ExecutionException {
            return select(key, new Condition(){
                public boolean reject(String value){
                    // not contains reject
                    if(args.contains(value)){
                        return false;
                    }
                    return true;
                }
            });
        }

        /**
         * [1, 2, 3, 4, 5] not arg(1, 2) => results(3, 4, 5)
         */
        public List<String> not(final String key, final List<String> args) throws ExecutionException {
            return select(key, new Condition(){
                public boolean reject(String value){
                    // contains reject
                    return args.contains(value);
                }
            });
        }

        /**
         * [1, 2, 3, 4, 5] or arg(4, 5, 6) => results(1, 2, 3, 4, 5)
         */
        public List<String> or(final String key, final List<String> args) throws ExecutionException {
            return select(key, new Filter(){
                public List<String> execute(List<String> results){
                    // no filter
                    return results;
                }
            });
        }
    }
    
    protected static interface Condition {
        public boolean reject(String value);
    }
    
    protected static interface Filter {
        public List<String> execute(List<String> results);
    }
    
}
