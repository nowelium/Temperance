package temperance.handler.function;

import java.util.ArrayList;
import java.util.List;

import libmemcached.exception.LibMemcachedException;

import temperance.ft.Gram;
import temperance.ft.Hashing;
import temperance.handler.function.exception.ExecutionException;
import temperance.ql.InternalFunction;
import temperance.storage.MemcachedFullText;
import temperance.util.ListUtils;

public class GramFunction implements InternalFunction {
    
    protected static final int SPLIT = 1000;
    
    protected final FunctionContext context;
    
    protected final int initialSeparator;
    
    public GramFunction(FunctionContext context){
        this(context, 2);
    }
    
    public GramFunction(FunctionContext context, int separatorValue){
        this.context = context;
        this.initialSeparator = separatorValue;
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
        
        final int split = getSplitValue(args);
        final String str = args.get(0);
        MemcachedFullText list = new MemcachedFullText(context.getClient());

        try {
            Hashing gram = new Gram(context.getHashFunction(), split);
            List<String> returnValues = new ArrayList<String>();
            
            List<Long> hashes = gram.parse(str);
            for(Long hash: hashes){
                long count = list.count(key, hash);
                for(long i = 0; i < count; i += SPLIT){
                    returnValues.addAll(list.get(key, hash, i, SPLIT));
                }
            }
            return ListUtils.unique(returnValues);
        } catch(LibMemcachedException e){
            throw new ExecutionException(e);
        }
    }

    public List<String> selectNot(String key, List<String> args) throws ExecutionException {
        throw new ExecutionException("not yet implemented");
    }
    
    private int getSplitValue(List<String> args){
        if(args.size() < 2){
            return initialSeparator;
        }
        String splitStr = args.get(1);
        try {
            return Integer.parseInt(splitStr);
        } catch(NumberFormatException e){
            return initialSeparator;
        }
    }

}
