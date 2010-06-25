package temperance.exception;

import libmemcached.exception.LibMemcachedException;

public class MemcachedOperationException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public MemcachedOperationException(String message){
        super(message);
    }
    
    public MemcachedOperationException(LibMemcachedException e){
        super(e);
    }

}
