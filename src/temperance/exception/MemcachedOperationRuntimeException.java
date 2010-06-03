package temperance.exception;

public class MemcachedOperationRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public MemcachedOperationRuntimeException(String message){
        super(message);
    }
    
    public MemcachedOperationRuntimeException(Exception parent){
        super(parent);
    }

}
