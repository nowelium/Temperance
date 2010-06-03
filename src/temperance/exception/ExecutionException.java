package temperance.exception;

public class ExecutionException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public ExecutionException(String message){
        super(message);
    }
    
    public ExecutionException(Exception parent){
        super(parent);
    }
    
}
