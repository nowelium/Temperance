package temperance.storage;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.MemcachedClient;

public class MemcachedMap {
    
    protected static final int flag = 0;

    protected final MemcachedClient client;
    
    public MemcachedMap(MemcachedClient client){
        this.client = client;
    }
    
    public void set(String key, String value, int expire) throws LibMemcachedException {
        client.getStorage().set(key, value, expire, flag);
    }
    
    public String get(String key) throws LibMemcachedException {
        return client.getStorage().get(key);
    }
}
