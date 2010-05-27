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

    public List<String> in(String key, List<String> args) {
        return null;
    }

    public List<String> not(String key, List<String> args) {
        return null;
    }

}
