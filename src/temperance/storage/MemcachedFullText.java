package temperance.storage;

import java.util.List;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.MemcachedStorage;
import temperance.core.ConnectionPool;
import temperance.storage.MemcachedList.KeyCache;
import temperance.util.Lists;

public class MemcachedFullText {
    
    protected static final KeyCache<String> hashKeyCache = new KeyCache<String>();
    
    protected static final String KEY_SEPARATOR = "#";
    
    protected final ConnectionPool pool;
    
    protected final MemcachedList list;
    
    public MemcachedFullText(ConnectionPool pool) {
        this.pool = pool;
        this.list = new MemcachedList(pool);
    }
    
    public long add(final String key, final Long hash, final String value, final int expire) throws LibMemcachedException {
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            MemcachedList.append(storage, key, hash.toString(), expire);
            
            return MemcachedList.append(storage, genKey(key, hash), value, expire);
        } finally {
            pool.release(client);
        }
    }
    
    public List<Long> addAll(final String key, final List<Long> hashes, final String value, final int expire) throws LibMemcachedException {
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            final List<Long> returnValue = Lists.newArrayList();
            for(Long hash: hashes){
                MemcachedList.append(storage, key, hash.toString(), expire);
                
                long nexthashId = MemcachedList.append(storage, genKey(key, hash), value, expire);
                returnValue.add(Long.valueOf(nexthashId));
            }
            return returnValue;
        } finally {
            pool.release(client);
        }
    }
    
    public List<Long> getHash(final String key, final long offset, final long limit) throws LibMemcachedException {
        final List<String> hashStringList = list.get(key, offset, limit);
        final List<Long> hashLongList = Lists.newArrayList();
        for(String hash: hashStringList){
            hashLongList.add(Long.valueOf(hash));
        }
        return hashLongList;
    }
    
    public List<String> get(final String key, final Long hash, final long offset, final long limit) throws LibMemcachedException {
        return list.get(genKey(key, hash), offset, limit);
    }
    
    public long count(final String key) throws LibMemcachedException {
        return list.count(key);
    }
    
    public long count(final String key, final Long hash) throws LibMemcachedException {
        return list.count(genKey(key, hash));
    }

    protected static String genKey(final String key, final Long hash){
        String hashKey = hashKeyCache.get(key, hash);
        if(null == hashKey){
            hashKey = new StringBuffer(key).append(KEY_SEPARATOR).append(hash.toString()).toString();
            hashKeyCache.put(key, hash, hashKey);
        }
        return hashKey;
    }

}
