package temperance.storage;

import temperance.exception.LockTimeoutException;
import temperance.exception.MemcachedOperationException;

public interface TpUUID {
    
    public long generate(String key) throws MemcachedOperationException, LockTimeoutException;

}
