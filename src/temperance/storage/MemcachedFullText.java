package temperance.storage;

import java.util.List;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.MemcachedClient;
import temperance.util.Lists;

public class MemcachedFullText {
    
    protected final MemcachedList list;

    public MemcachedFullText(MemcachedClient client) {
        this.list = new MemcachedList(client);
    }
    
    public String add(String key, Long hash, String value, int expire) throws LibMemcachedException {
        list.add(key, hash.toString(), expire);
        return list.add(genKey(key, hash), value, expire);
    }
    
    public List<Long> get(String key, long offset, long limit) throws LibMemcachedException {
        List<String> hashStringList = list.get(key, offset, limit);
        List<Long> hashLongList = Lists.newArrayList();
        for(String hash: hashStringList){
            hashLongList.add(Long.valueOf(hash));
        }
        return hashLongList;
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
