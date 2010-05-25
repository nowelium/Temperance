package temperance.storage;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.MemcachedClient;

public class MemcachedMap {
    protected static final String KEY_SEPARATOR = ":";
    
    protected static final int expiration = 0;
    
    protected static final int flag = 0;

    protected final MemcachedClient client;
    
    protected final String namespace;

    public MemcachedMap(MemcachedClient client, String namespace){
        this.client = client;
        this.namespace = namespace;
    }
    
    public void set(String key, String value) throws LibMemcachedException {
        String storeKey = namespace + KEY_SEPARATOR + key;
        client.getStorage().set(storeKey, value, expiration, flag);
    }
    
    public String get(String key) throws LibMemcachedException {
        String storeKey = namespace + KEY_SEPARATOR + key;
        return client.getStorage().get(storeKey);
    }
}
