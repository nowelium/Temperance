package temperance.storage;

import java.util.List;

import libmemcached.exception.LibMemcachedException;

public interface FullText {
    
    public long add(String key, Long hash, String value, int expire) throws LibMemcachedException;
    
    public List<Long> addAll(String key, List<Long> hashes, String value, int expire) throws LibMemcachedException;
    
    public List<Long> getHashes(String key, long offset, long limit) throws LibMemcachedException;
    
    public List<String> getValues(String key, Long hash, long offset, long limit) throws LibMemcachedException;
    
    public long hashCount(String key) throws LibMemcachedException;
    
    public long valueCount(String key, Long hash) throws LibMemcachedException;
    
    public boolean delete(String key, int expire) throws LibMemcachedException;
    
    public boolean deleteByHash(String key, Long hash, int expire) throws LibMemcachedException;
    
    public boolean deleteAtByHash(String key, Long hash, long index, int expire) throws LibMemcachedException;
    
}
