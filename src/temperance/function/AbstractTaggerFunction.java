package temperance.function;

import java.util.List;
import java.util.concurrent.Future;

import temperance.exception.ExecutionException;
import temperance.ft.Hashing;
import temperance.memcached.FullTextCommand;
import temperance.storage.MemcachedFullText;
import temperance.util.Lists;
import temperance.util.Lists.IntersectalList;

public abstract class AbstractTaggerFunction implements InternalFunction {
    
    protected static final int SPLIT = 3000;

    protected final FunctionContext context;
    
    protected final MemcachedFullText ft;
    
    public AbstractTaggerFunction(FunctionContext context){
        this.context = context;
        this.ft = new MemcachedFullText(context.getPool());
    }
    
    protected abstract Hashing createHashing(List<String> args);
    
    public Command createDelete(){
        return new Delete();
    }
    
    public Command createSelect(){
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
            if(args.isEmpty()){
                throw new ExecutionException("arguments was empty");
            }
            
            final String str = args.get(0);
            final Hashing hashing = createHashing(args);
            try {
                IntersectalList<String> returnValue = Lists.newIntersectList();
                List<Long> allHashes = hashing.parse(str);
                
                FullTextCommand command = new FullTextCommand(context.getPool());
                List<Future<List<String>>> futures = command.getAll(key, allHashes);
                for(Future<List<String>> future: futures){
                    List<String> results = future.get();
                    returnValue.intersect(results);
                }
                
                return returnValue;
            } catch (InterruptedException e) {
                throw new ExecutionException(e);
            } catch (java.util.concurrent.ExecutionException e) {
                throw new ExecutionException(e);
            }
        }

        public List<String> not(String key, List<String> args) throws ExecutionException {
            if(args.isEmpty()){
                throw new ExecutionException("arguments was empty");
            }
            
            final String str = args.get(0);
            final Hashing hashing = createHashing(args);
            try {
                List<Long> ignoreHashes = hashing.parse(str);
                FullTextCommand command = new FullTextCommand(context.getPool());
                
                List<Long> allKeys = command.getAll(key).get();
                // remove ignore keys
                allKeys.removeAll(ignoreHashes);
                
                List<String> returnValue = Lists.newArrayList();
                List<Future<List<String>>> futures = command.getAll(key, allKeys);
                for(Future<List<String>> future: futures){
                    List<String> result = future.get();
                    returnValue.addAll(result);
                }
                return returnValue;
            } catch (InterruptedException e) {
                throw new ExecutionException(e);
            } catch (java.util.concurrent.ExecutionException e) {
                throw new ExecutionException(e);
            }
        }

        public List<String> or(String key, List<String> args) throws ExecutionException {
            throw new ExecutionException("not yet implemented");
        }
    }
    
}
