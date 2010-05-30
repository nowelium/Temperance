package temperance.handler.function;

import java.util.List;

import temperance.ql.InternalFunction;

public class GramFunction implements InternalFunction {
    
    protected final FunctionContext context;
    
    protected final int initialSeparator;
    
    public GramFunction(FunctionContext context){
        this(context, 2);
    }
    
    public GramFunction(FunctionContext context, int separatorValue){
        this.context = context;
        this.initialSeparator = separatorValue;
    }

    public List<String> deleteIn(String key, List<String> args) {
        return null;
    }

    public List<String> deleteNot(String key, List<String> args) {
        return null;
    }

    public List<String> selectIn(String key, List<String> args) {
        return null;
    }

    public List<String> selectNot(String key, List<String> args) {
        return null;
    }

}
