package temperance.function.impl;

import java.util.List;

import temperance.exception.CommandExecutionException;
import temperance.function.FunctionContext;
import temperance.function.InternalFunction;

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
        public List<String> and(String key, List<String> args) throws CommandExecutionException {
            throw new CommandExecutionException("not yet implemented");
        }

        public List<String> not(String key, List<String> args) throws CommandExecutionException {
            throw new CommandExecutionException("not yet implemented");
        }

        public List<String> or(String key, List<String> args) throws CommandExecutionException {
            throw new CommandExecutionException("not yet implemented");
        }
    }
    
    protected class Select implements InternalFunction.Command {
        public List<String> and(String key, List<String> args) throws CommandExecutionException {
            throw new CommandExecutionException("not yet implemented");
        }

        public List<String> not(String key, List<String> args) throws CommandExecutionException {
            throw new CommandExecutionException("not yet implemented");
        }

        public List<String> or(String key, List<String> args) throws CommandExecutionException {
            throw new CommandExecutionException("not yet implemented");
        }
    }
}
