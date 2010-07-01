package temperance.storage;

import temperance.exception.LockTimeoutException;
import temperance.exception.MemcachedOperationException;

public interface TpUUID extends TpStorage {
    
    public long generate(String key) throws MemcachedOperationException, LockTimeoutException;

}
