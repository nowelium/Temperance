package temperance.exception;

public class LockTimeoutException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public LockTimeoutException(String message){
        super(message);
    }
    
    public LockTimeoutException(Exception e){
        super(e);
    }

}
