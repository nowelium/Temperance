package temperance.function.impl;

import java.util.List;

import temperance.core.ListCommand;
import temperance.exception.CommandExecutionException;
import temperance.function.FunctionContext;
import temperance.function.InternalFunction;
import temperance.util.Lists;

public class ValueFunction implements InternalFunction {
    
    protected final FunctionContext context;
    
    public ValueFunction(FunctionContext context){
        this.context = context;
    }
    
    public Command createDelete() {
        return new Delete();
    }

    public Command createSelect() {
        return new Select();
    }
    
    protected List<String> select(final String key, final Condition condition) throws CommandExecutionException {
        return select(key, new Filter() {
            public List<String> execute(List<String> results){
                List<String> returnValue = Lists.newArrayList();
                for(String value: results){
                    if(condition.reject(value)){
                        continue;
                    }
                    returnValue.add(value);
                }
                return returnValue;
            }
        });
    }
    
    protected List<String> select(final String key, final Filter filter) throws CommandExecutionException {
        try {
            final ListCommand command = new ListCommand(context.getPooling());
            final List<String> returnValue = Lists.newArrayList();
            command.filterAll(key, new ListCommand.Filter(){
                public void execute(List<String> values){
                    synchronized(returnValue){
                        returnValue.addAll(filter.execute(values));
                    }
                }
            });
            return returnValue;
        } catch (InterruptedException e) {
            throw new CommandExecutionException(e);
        }
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
         * [1, 2, 3, 4, 5] in arg(1, 2) => results(1, 2)
         */
        public List<String> and(final String key, final List<String> args) throws CommandExecutionException {
            return select(key, new Condition(){
                public boolean reject(String value){
                    // not contains reject
                    if(args.contains(value)){
                        return false;
                    }
                    return true;
                }
            });
        }

        /**
         * [1, 2, 3, 4, 5] not arg(1, 2) => results(3, 4, 5)
         */
        public List<String> not(final String key, final List<String> args) throws CommandExecutionException {
            return select(key, new Condition(){
                public boolean reject(String value){
                    // contains reject
                    if(args.contains(value)){
                        return true;
                    }
                    return false;
                }
            });
        }

        /**
         * [1, 2, 3, 4, 5] or arg(4, 5, 6) => results(1, 2, 3, 4, 5)
         */
        public List<String> or(final String key, final List<String> args) throws CommandExecutionException {
            return select(key, new Filter(){
                public List<String> execute(List<String> results){
                    // no filter
                    return results;
                }
            });
        }
    }
    
    protected static interface Condition {
        public boolean reject(String value);
    }
    
    protected static interface Filter {
        public List<String> execute(List<String> results);
    }
    
}
