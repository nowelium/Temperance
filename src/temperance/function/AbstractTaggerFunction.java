package temperance.function;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import temperance.core.FullTextCommand;
import temperance.exception.CommandExecutionException;
import temperance.hashing.Hashing;
import temperance.util.Lists;
import temperance.util.Lists.IntersectalList;

public abstract class AbstractTaggerFunction implements InternalFunction {
    
    protected final FunctionContext context;
    
    public AbstractTaggerFunction(FunctionContext context){
        this.context = context;
    }
    
    protected abstract Hashing createHashing(List<String> args);
    
    public Command createDelete(){
        return new Delete();
    }
    
    public Command createSelect(){
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
            if(args.isEmpty()){
                throw new CommandExecutionException("arguments was empty");
            }
            
            final String str = args.get(0);
            final Hashing hashing = createHashing(args);
            try {
                IntersectalList<String> returnValue = Lists.newIntersectList();
                List<Long> allHashes = hashing.parse(str);
                
                FullTextCommand command = new FullTextCommand(context.getPooling());
                List<Future<List<String>>> futures = command.getValues(key, allHashes);
                for(Future<List<String>> future: futures){
                    List<String> results = future.get();
                    returnValue.intersect(results);
                }
                
                return returnValue.getValues();
            } catch (InterruptedException e) {
                throw new CommandExecutionException(e);
            } catch (ExecutionException e) {
                throw new CommandExecutionException(e);
            }
        }

        public List<String> not(String key, List<String> args) throws CommandExecutionException {
            if(args.isEmpty()){
                throw new CommandExecutionException("arguments was empty");
            }
            
            final String str = args.get(0);
            final Hashing hashing = createHashing(args);
            try {
                List<Long> ignoreHashes = hashing.parse(str);
                FullTextCommand command = new FullTextCommand(context.getPooling());
                
                List<Long> allKeys = command.getHashes(key).get();
                // remove ignore keys
                allKeys.removeAll(ignoreHashes);
                
                List<String> returnValue = Lists.newArrayList();
                List<Future<List<String>>> futures = command.getValues(key, allKeys);
                for(Future<List<String>> future: futures){
                    List<String> result = future.get();
                    returnValue.addAll(result);
                }
                return returnValue;
            } catch (InterruptedException e) {
                throw new CommandExecutionException(e);
            } catch (ExecutionException e) {
                throw new CommandExecutionException(e);
            }
        }

        public List<String> or(String key, List<String> args) throws CommandExecutionException {
            throw new CommandExecutionException("not yet implemented");
        }
    }
    
}
