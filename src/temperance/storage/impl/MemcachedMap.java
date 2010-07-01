package temperance.storage.impl;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.type.ReturnType;
import temperance.core.ConnectionPool;
import temperance.storage.TpMap;

public class MemcachedMap implements TpMap {
    
    protected static final String DEFAULT_ROOT_KEY_PREFIX = TpMap.class.getSimpleName();
    
    protected final ConnectionPool pool;
    
    public MemcachedMap(ConnectionPool pool){
        this.pool = pool;
    }
    
    public boolean set(String key, String value, int expire) throws LibMemcachedException {
        final MemcachedClient client = pool.get();
        try {
            ReturnType rt = client.getStorage().set(genKey(key), value, expire, DEFAULT_VALUE_FLAG);
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
            return client.getStorage().get(genKey(key));
        } finally {
            pool.release(client);
        }
    }
    
    public boolean delete(String key, int expire) throws LibMemcachedException {
        final MemcachedClient client = pool.get();
        try {
            ReturnType rt = client.getStorage().delete(genKey(key), expire);
            if(ReturnType.SUCCESS.equals(rt)){
                return true;
            }
            return false;
        } finally {
            pool.release(client);
        }
    }
    
    protected static String genKey(String key){
        return DEFAULT_ROOT_KEY_PREFIX.concat(KEY_SEPARATOR).concat(key);
    }
}
