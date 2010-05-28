package temperance.handler.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import libmemcached.exception.LibMemcachedException;
import temperance.ql.InternalFunction;
import temperance.storage.MemcachedList;

public class ValueFunction implements InternalFunction {
    
    protected static final int SPLIT = 1000;
    
    protected final FunctionContext context;
    
    public ValueFunction(FunctionContext context){
        this.context = context;
    }

    /**
     * [1, 2, 3, 4, 5] in arg(1, 2) => results(1, 2)
     */
    public List<String> in(String key, List<String> args) {
        try {
            MemcachedList list = new MemcachedList(context.getClient());
            List<String> returnValue = new ArrayList<String>();
            
            Condition condition = new NotContainsReject(args);
            long count = list.count(key);
            for(long i = 0; i < count; i += SPLIT){
                List<String> results = list.get(key, i, SPLIT);
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
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * [1, 2, 3, 4, 5] not arg(1, 2) => results(3, 4, 5)
     */
    public List<String> not(String key, List<String> args) {
        try {
            MemcachedList list = new MemcachedList(context.getClient());
            List<String> returnValue = new ArrayList<String>();
            
            Condition condition = new ContainsReject(args);
            long count = list.count(key);
            for(long i = 0; i < count; i += SPLIT){
                List<String> results = list.get(key, i, SPLIT);
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
            e.printStackTrace();
            return Collections.emptyList();
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
