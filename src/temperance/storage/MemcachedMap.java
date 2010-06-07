package temperance.storage;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.MemcachedClient;
import temperance.memcached.ConnectionPool;

public class MemcachedMap {
    
    protected static final int flag = 0;

    protected final ConnectionPool pool;
    
    public MemcachedMap(ConnectionPool pool){
        this.pool = pool;
    }
    
    public void set(String key, String value, int expire) throws LibMemcachedException {
        final MemcachedClient client = pool.get();
        try {
            client.getStorage().set(key, value, expire, flag);
        } finally {
            pool.release(client);
        }
    }
    
    public String get(String key) throws LibMemcachedException {
        final MemcachedClient client = pool.get();
        try {
            return client.getStorage().get(key);
        } finally {
            pool.release(client);
        }
    }
}
