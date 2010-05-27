package temperance.handler.function;

import java.util.Collections;
import java.util.List;

import libmemcached.exception.LibMemcachedException;

import temperance.ql.InternalFunction;
import temperance.storage.MemcachedList;

public class ValueFunction implements InternalFunction {
    
    protected final FunctionContext context;
    
    public ValueFunction(FunctionContext context){
        this.context = context;
    }

    public List<String> in(String key, List<String> args) {
        try {
            MemcachedList list = new MemcachedList(context.getClient());
            return list.includeIndex(key, args);
        } catch(LibMemcachedException e){
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<String> not(String key, List<String> args) {
        try {
            MemcachedList list = new MemcachedList(context.getClient());
            return list.excludeIndex(key, args);
        } catch(LibMemcachedException e){
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

}
