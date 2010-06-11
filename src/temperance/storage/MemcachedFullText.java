package temperance.storage;

import java.util.List;

import libmemcached.exception.LibMemcachedException;
import temperance.core.ConnectionPool;
import temperance.storage.MemcachedList.KeyCache;
import temperance.util.Lists;

public class MemcachedFullText {
    
    protected static final KeyCache<String> hashKeyCache = new KeyCache<String>();
    
    protected static final String KEY_SEPARATOR = "#";
    
    protected final MemcachedList list;
    
    public MemcachedFullText(ConnectionPool pool) {
        this.list = new MemcachedList(pool);
    }
    
    public long add(String key, Long hash, String value, int expire) throws LibMemcachedException {
        list.add(key, hash.toString(), expire);
        return list.add(genKey(key, hash), value, expire);
    }
    
    public List<Long> getHash(String key, long offset, long limit) throws LibMemcachedException {
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
        if(hashKeyCache.contains(key, hash)){
            return hashKeyCache.get(key, hash);
        }
        
        String hashKey = new StringBuffer(key).append(KEY_SEPARATOR).append(hash.toString()).toString();
        hashKeyCache.put(key, hash, hashKey);
        return hashKey;
    }

}
