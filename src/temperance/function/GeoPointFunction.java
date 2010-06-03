package temperance.function;

import java.util.List;

import temperance.exception.ExecutionException;

public class GeoPointFunction implements InternalFunction {

    protected final FunctionContext context;
    
    public GeoPointFunction(FunctionContext context){
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
}
