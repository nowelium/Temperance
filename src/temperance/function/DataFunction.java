package temperance.function;

import java.util.List;
import java.util.concurrent.Future;

import temperance.exception.ExecutionException;
import temperance.memcached.ListCommand;

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
        /**
         * DATA(1, 2, 3, 4, 5) in DATA(2, 3, 5) => results(2, 3, 5)
         */
        public List<String> and(String key, List<String> args) throws ExecutionException {
            final ListCommand command = new ListCommand(context.getPool());
            
            final Future<List<String>> fromFuture = command.getAll(key);
            try {
                List<String> returnValues = fromFuture.get();
                List<Future<List<String>>> futures = command.getAll(args);
                for(Future<List<String>> future: futures){
                    List<String> results = future.get();
                    
                    // narrow
                    // fromValues contains all(only) tagetValues
                    returnValues.retainAll(results);
                }
                return returnValues;
            } catch (InterruptedException e) {
                throw new ExecutionException(e);
            } catch (java.util.concurrent.ExecutionException e) {
                throw new ExecutionException(e);
            }
        }

        /**
         * DATA(1, 2, 3, 4, 5) not DATA(2, 3, 5) => results(1, 4)
         */
        public List<String> not(String key, List<String> args) throws ExecutionException {
            final ListCommand command = new ListCommand(context.getPool());
            
            final Future<List<String>> fromFuture = command.getAll(key);
            try {
                List<String> returnValues = fromFuture.get();
                List<Future<List<String>>> futures = command.getAll(args);
                for(Future<List<String>> future: futures){
                    List<String> results = future.get();
                    for(String result: results){
                        // contains remove
                        returnValues.remove(result);
                    }
                }
                return returnValues;
            } catch (InterruptedException e) {
                throw new ExecutionException(e);
            } catch (java.util.concurrent.ExecutionException e) {
                throw new ExecutionException(e);
            }
        }

        /**
         * DATA(1, 2, 3, 4, 5) or DATA(4, 5, 6, 7) => result(1, 2, 3, 4, 5, 6, 7)
         */
        public List<String> or(String key, List<String> args) throws ExecutionException {
            final ListCommand command = new ListCommand(context.getPool());
            
            final Future<List<String>> fromFuture = command.getAll(key);
            try {
                List<String> returnValue = fromFuture.get();
                
                List<Future<List<String>>> futures = command.getAll(args);
                for(Future<List<String>> future: futures){
                    returnValue.addAll(future.get());
                }
                return returnValue;
            } catch (InterruptedException e) {
                throw new ExecutionException(e);
            } catch (java.util.concurrent.ExecutionException e) {
                throw new ExecutionException(e);
            }
        }
    }
    
}
