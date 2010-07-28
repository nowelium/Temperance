package temperance.storage;

import temperance.exception.MemcachedOperationException;

public interface TpQueue extends TpStorage {

    public boolean enqueue(String key, String value, int expire);
    
    public String dequeue(String key) throws MemcachedOperationException;
}
