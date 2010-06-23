package temperance.storage.impl;

import java.util.List;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.MemcachedStorage;
import temperance.core.ConnectionPool;
import temperance.storage.FullText;
import temperance.storage.Sequence.SequenceResult;
import temperance.storage.impl.MemcachedSequence.KeyCache;
import temperance.util.Lists;

public class MemcachedFullText implements FullText {
    
    protected static final KeyCache<String> hashKeyCache = new KeyCache<String>();
    
    protected static final String DEFAULT_ROOT_KEY_PREFIX = "fulltext";
    
    protected static final String KEY_SEPARATOR = "#";
    
    protected final ConnectionPool pool;
    
    protected final MemcachedSequence sequence;
    
    public MemcachedFullText(ConnectionPool pool) {
        this.pool = pool;
        this.sequence = new MemcachedSequence(pool, DEFAULT_ROOT_KEY_PREFIX);
    }
    
    public long add(final String key, final Long hash, final String value, final int expire) throws LibMemcachedException {
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            sequence.append(storage, key, hash.toString(), expire);
            
            return sequence.append(storage, genKey(key, hash), value, expire);
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
                sequence.append(storage, key, hash.toString(), expire);
                
                long nexthashId = sequence.append(storage, genKey(key, hash), value, expire);
                returnValue.add(Long.valueOf(nexthashId));
            }
            return returnValue;
        } finally {
            pool.release(client);
        }
    }
    
    public List<Long> getHashes(final String key, final long offset, final long limit) throws LibMemcachedException {
        final List<SequenceResult> hashResults = getHashesByResult(key, offset, limit);
        final List<Long> hashList = Lists.newArrayList();
        for(SequenceResult result: hashResults){
            hashList.add(Long.valueOf(result.getValue()));
        }
        return hashList;
    }
    
    public List<SequenceResult> getHashesByResult(final String key, final long offset, final long limit) throws LibMemcachedException {
        return sequence.getByResult(key, offset, limit);
    }
    
    public List<String> getValues(final String key, final Long hash, final long offset, final long limit) throws LibMemcachedException {
        final List<SequenceResult> valueResults = getValuesByResult(key, hash, offset, limit);
        final List<String> valueList = Lists.newArrayList();
        for(SequenceResult result: valueResults){
            valueList.add(result.getValue());
        }
        return valueList;
    }
    
    public List<SequenceResult> getValuesByResult(final String key, final Long hash, final long offset, final long limit) throws LibMemcachedException {
        return sequence.getByResult(genKey(key, hash), offset, limit);
    }
    
    public long hashCount(final String key) throws LibMemcachedException {
        return sequence.count(key);
    }
    
    public long valueCount(final String key, final Long hash) throws LibMemcachedException {
        return sequence.count(genKey(key, hash));
    }
    
    public boolean delete(final String key, final int expire) throws LibMemcachedException {
        return sequence.delete(key, expire);
    }
    
    public boolean deleteByHash(final String key, final Long hash, final int expire) throws LibMemcachedException {
        return sequence.delete(genKey(key, hash), expire);
    }
    
    public boolean deleteAtByHash(final String key, final Long hash, final long index, final int expire) throws LibMemcachedException {
        return sequence.deleteAt(genKey(key, hash), index, expire);
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
