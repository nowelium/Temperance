package temperance.storage.impl;

import java.util.List;

import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.MemcachedStorage;
import temperance.core.ConnectionPool;
import temperance.exception.LockTimeoutException;
import temperance.exception.MemcachedOperationException;
import temperance.hash.Hash;
import temperance.hash.StringHash;
import temperance.storage.TpFullText;
import temperance.storage.TpList.SequenceResult;
import temperance.storage.impl.MemcachedList.KeyCache;
import temperance.util.Lists;

public class MemcachedFullText implements TpFullText {
    
    protected static final KeyCache<String, Hash> hashKeyCache = new KeyCache<String, Hash>();
    
    protected static final String DEFAULT_ROOT_KEY_PREFIX = "fulltext";
    
    protected static final String KEY_SEPARATOR = "#";
    
    protected final ConnectionPool pool;
    
    protected final MemcachedList sequence;
    
    public MemcachedFullText(ConnectionPool pool) {
        this.pool = pool;
        this.sequence = new MemcachedList(pool, DEFAULT_ROOT_KEY_PREFIX);
    }
    
    public long add(final String key, final Hash hash, final String value, final int expire) throws MemcachedOperationException, LockTimeoutException {
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            sequence.append(storage, key, hash.toString(), expire);
            
            return sequence.append(storage, genKey(key, hash), value, expire);
        } finally {
            pool.release(client);
        }
    }
    
    public List<Long> addAll(final String key, final List<Hash> hashes, final String value, final int expire) throws MemcachedOperationException, LockTimeoutException {
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            final List<Long> returnValue = Lists.newArrayList();
            for(Hash hash: hashes){
                sequence.append(storage, key, hash.toString(), expire);
                
                long nexthashId = sequence.append(storage, genKey(key, hash), value, expire);
                returnValue.add(Long.valueOf(nexthashId));
            }
            return returnValue;
        } finally {
            pool.release(client);
        }
    }
    
    public List<Hash> getHashes(final String key, final long offset, final long limit) throws MemcachedOperationException {
        final List<SequenceResult> hashResults = getHashesByResult(key, offset, limit);
        final List<Hash> hashList = Lists.newArrayList();
        for(SequenceResult result: hashResults){
            hashList.add(new StringHash(result.getValue()));
        }
        return hashList;
    }
    
    public List<SequenceResult> getHashesByResult(final String key, final long offset, final long limit) throws MemcachedOperationException {
        return sequence.getByResult(key, offset, limit);
    }
    
    public List<String> getValues(final String key, final Hash hash, final long offset, final long limit) throws MemcachedOperationException {
        final List<SequenceResult> valueResults = getValuesByResult(key, hash, offset, limit);
        final List<String> valueList = Lists.newArrayList();
        for(SequenceResult result: valueResults){
            valueList.add(result.getValue());
        }
        return valueList;
    }
    
    public List<SequenceResult> getValuesByResult(final String key, final Hash hash, final long offset, final long limit) throws MemcachedOperationException {
        return sequence.getByResult(genKey(key, hash), offset, limit);
    }
    
    public long hashCount(final String key) throws MemcachedOperationException {
        return sequence.count(key);
    }
    
    public long valueCount(final String key, final Hash hash) throws MemcachedOperationException {
        return sequence.count(genKey(key, hash));
    }
    
    public boolean delete(final String key, final int expire) throws MemcachedOperationException, LockTimeoutException {
        return sequence.delete(key, expire);
    }
    
    public boolean deleteByHash(final String key, final Hash hash, final int expire) throws MemcachedOperationException, LockTimeoutException {
        return sequence.delete(genKey(key, hash), expire);
    }
    
    public boolean deleteAtByHash(final String key, final Hash hash, final long index, final int expire) throws MemcachedOperationException, LockTimeoutException {
        return sequence.deleteAt(genKey(key, hash), index, expire);
    }
    
    public void reindex(final String key) throws MemcachedOperationException, LockTimeoutException {
        sequence.reindex(key);
    }
    
    public void reindex(final String key, final Hash hash) throws MemcachedOperationException, LockTimeoutException {
        sequence.reindex(genKey(key, hash));
    }

    protected static String genKey(final String key, final Hash hash){
        String hashKey = hashKeyCache.get(key, hash);
        if(null == hashKey){
            hashKey = new StringBuffer(key).append(KEY_SEPARATOR).append(hash.hashValue()).toString();
            hashKeyCache.put(key, hash, hashKey);
        }
        return hashKey;
    }

}
