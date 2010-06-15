package temperance.storage;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.type.ReturnType;
import temperance.core.ConnectionPool;

public class MemcachedMap {
    
    protected static final int flag = 0;

    protected final ConnectionPool pool;
    
    public MemcachedMap(ConnectionPool pool){
        this.pool = pool;
    }
    
    public boolean set(String key, String value, int expire) throws LibMemcachedException {
        final MemcachedClient client = pool.get();
        try {
            ReturnType rt = client.getStorage().set(key, value, expire, flag);
            if(ReturnType.BUFFERED.equals(rt) || ReturnType.SUCCESS.equals(rt)){
                return true;
            }
            return false;
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
    
    public boolean delete(String key, int expire) throws LibMemcachedException {
        final MemcachedClient client = pool.get();
        try {
            ReturnType rt = client.getStorage().delete(key, expire);
            if(ReturnType.SUCCESS.equals(rt)){
                return true;
            }
            return false;
        } finally {
            pool.release(client);
        }
    }
}
