package temperance.handler.function;

import java.util.List;

import temperance.ql.InternalFunction;

public class DataFunction implements InternalFunction {
    
    protected final FunctionContext context;
    
    public DataFunction(FunctionContext context){
        this.context = context;
    }

    public List<String> in(String key, List<String> args) {
        return null;
    }

    public List<String> not(String key, List<String> args) {
        return null;
    }

}
