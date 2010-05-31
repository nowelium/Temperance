package temperance.handler.function;

import java.util.List;

import temperance.handler.function.exception.ExecutionException;
import temperance.ql.InternalFunction;

public class GeoPointFunction implements InternalFunction {

    protected final FunctionContext context;
    
    public GeoPointFunction(FunctionContext context){
        this.context = context;
    }

    public List<String> deleteIn(String key, List<String> args) throws ExecutionException {
        throw new ExecutionException("not yet implemented");
    }

    public List<String> deleteNot(String key, List<String> args) throws ExecutionException {
        throw new ExecutionException("not yet implemented");
    }

    public List<String> selectIn(String key, List<String> args) throws ExecutionException {
        throw new ExecutionException("not yet implemented");
    }

    public List<String> selectNot(String key, List<String> args) throws ExecutionException {
        throw new ExecutionException("not yet implemented");
    }

}
