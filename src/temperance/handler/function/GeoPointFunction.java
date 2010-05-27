package temperance.handler.function;

import java.util.List;

import temperance.ql.InternalFunction;

public class GeoPointFunction implements InternalFunction {

    protected final FunctionContext context;
    
    public GeoPointFunction(FunctionContext context){
        this.context = context;
    }
    
    public List<String> in(String key, List<String> args) {
        return null;
    }

    public List<String> not(String key, List<String> args) {
        return null;
    }

}
