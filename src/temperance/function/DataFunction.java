package temperance.function;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import temperance.core.SequenceCommand;
import temperance.exception.CommandExecutionException;

public class DataFunction implements InternalFunction {
    
    protected final FunctionContext context;
    
    public DataFunction(FunctionContext context){
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
        /**
         * DATA(1, 2, 3, 4, 5) in DATA(2, 3, 5) => results(2, 3, 5)
         */
        public List<String> and(String key, List<String> args) throws CommandExecutionException {
            final SequenceCommand command = new SequenceCommand(context.getPooling());
            
            final Future<List<String>> fromFuture = command.getAll(key);
            try {
                final List<String> returnValues = fromFuture.get();
                command.filterAll(args, new SequenceCommand.Filter(){
                    public void execute(List<String> values){
                        synchronized(returnValues){
                            // narrow
                            // fromValues contains all(only) tagetValues
                            returnValues.retainAll(values);
                        }
                    }
                });
                return returnValues;
            } catch (InterruptedException e) {
                throw new CommandExecutionException(e);
            } catch (ExecutionException e) {
                throw new CommandExecutionException(e);
            }
        }

        /**
         * DATA(1, 2, 3, 4, 5) not DATA(2, 3, 5) => results(1, 4)
         */
        public List<String> not(String key, List<String> args) throws CommandExecutionException {
            final SequenceCommand command = new SequenceCommand(context.getPooling());
            
            final Future<List<String>> fromFuture = command.getAll(key);
            try {
                final List<String> returnValues = fromFuture.get();
                command.filterAll(args, new SequenceCommand.Filter(){
                    public void execute(List<String> values){
                        synchronized(returnValues){
                            for(String value: values){
                                returnValues.remove(value);
                            }
                        }
                    }
                });
                return returnValues;
            } catch (InterruptedException e) {
                throw new CommandExecutionException(e);
            } catch (ExecutionException e) {
                throw new CommandExecutionException(e);
            }
        }

        /**
         * DATA(1, 2, 3, 4, 5) or DATA(4, 5, 6, 7) => result(1, 2, 3, 4, 5, 6, 7)
         */
        public List<String> or(String key, List<String> args) throws CommandExecutionException {
            final SequenceCommand command = new SequenceCommand(context.getPooling());
            
            final Future<List<String>> fromFuture = command.getAll(key);
            try {
                List<String> returnValue = fromFuture.get();
                
                List<Future<List<String>>> futures = command.getAll(args);
                for(Future<List<String>> future: futures){
                    returnValue.addAll(future.get());
                }
                return returnValue;
            } catch (InterruptedException e) {
                throw new CommandExecutionException(e);
            } catch (ExecutionException e) {
                throw new CommandExecutionException(e);
            }
        }
    }
    
}
