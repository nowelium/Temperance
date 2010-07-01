package temperance.storage.impl;

import libmemcached.wrapper.MemcachedClient;
import temperance.core.ConnectionPool;
import temperance.exception.LockTimeoutException;
import temperance.exception.MemcachedOperationException;
import temperance.storage.TpUUID;

public class MemcachedUUID implements TpUUID {
    
    protected static final String DEFAULT_ROOT_KEY_PREFIX = TpUUID.class.getSimpleName();
    
    protected static final String KEY_SEPARATOR = "$";

    protected final ConnectionPool pool;
    
    protected final MemcachedList list;
    
    public MemcachedUUID(ConnectionPool pool){
        this.pool = pool;
        this.list = new MemcachedList(pool, DEFAULT_ROOT_KEY_PREFIX);
    }

    public long generate(String key) throws MemcachedOperationException, LockTimeoutException {
        final MemcachedClient client = pool.get();
        try {
            return list.generateId(client.getStorage(), genKey(key));
        } finally {
            pool.release(client);
        }
    }
    
    protected static String genKey(String key){
        return DEFAULT_ROOT_KEY_PREFIX.concat(KEY_SEPARATOR).concat(key);
    }

}
