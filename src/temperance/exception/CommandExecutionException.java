package temperance.exception;

public class CommandExecutionException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public CommandExecutionException(String message){
        super(message);
    }
    
    public CommandExecutionException(Exception parent){
        super(parent);
    }
    
}
