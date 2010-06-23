package temperance.storage;

import libmemcached.exception.LibMemcachedException;

public interface Map {
    
    public boolean set(String key, String value, int expire) throws LibMemcachedException;
    
    public String get(String key) throws LibMemcachedException;
    
    public boolean delete(String key, int expire) throws LibMemcachedException;
}
