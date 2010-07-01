package temperance.storage;

import libmemcached.exception.LibMemcachedException;

public interface TpMap extends TpStorage {
    
    public boolean set(String key, String value, int expire) throws LibMemcachedException;
    
    public String get(String key) throws LibMemcachedException;
    
    public boolean delete(String key, int expire) throws LibMemcachedException;
}
