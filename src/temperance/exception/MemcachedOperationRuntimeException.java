package temperance.exception;

public class MemcachedOperationRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 3176176107503351474L;
    
    public MemcachedOperationRuntimeException(String message){
        super(message);
    }
    
    public MemcachedOperationRuntimeException(Exception e){
        super(e);
    }

}
