package temperance.function;

import java.util.List;

import temperance.exception.CommandExecutionException;

public interface InternalFunction {
    
    public Command createDelete();
    
    public Command createSelect();

    public interface Command {
        
        public List<String> and(String key, List<String> args) throws CommandExecutionException;
        
        public List<String> or(String key, List<String> args) throws CommandExecutionException;
        
        public List<String> not(String key, List<String> args) throws CommandExecutionException;
        
    }
}
