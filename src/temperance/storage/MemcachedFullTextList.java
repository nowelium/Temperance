package temperance.storage;

import java.util.List;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.MemcachedClient;

public class MemcachedFullTextList {
    
    protected final MemcachedList list;

    public MemcachedFullTextList(MemcachedClient client) {
        this.list = new MemcachedList(client);
    }
    
    public String add(String key, Long hash, String value) throws LibMemcachedException {
        list.add(key, hash.toString());
        return list.add(genKey(key, hash), value);
    }
    
    public List<String> get(String key, long offset, long limit) throws LibMemcachedException {
        return list.get(key, offset, limit);
    }
    
    public List<String> get(String key, Long hash, long offset, long limit) throws LibMemcachedException {
        return list.get(genKey(key, hash), offset, limit);
    }
    
    public long count(String key) throws LibMemcachedException {
        return list.count(key);
    }
    
    public long count(String key, Long hash) throws LibMemcachedException {
        return list.count(genKey(key, hash));
    }

    protected static String genKey(String key, Long hash){
        return new StringBuffer(key).append(MemcachedList.KEY_SEPARATOR).append(hash.toString()).toString();
    }

}
