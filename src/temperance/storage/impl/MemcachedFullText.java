package temperance.storage.impl;

import java.util.List;

import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.MemcachedStorage;
import temperance.core.ConnectionPool;
import temperance.exception.LockTimeoutException;
import temperance.exception.MemcachedOperationException;
import temperance.hash.Digest;
import temperance.hash.Hash;
import temperance.hash.HashFunction;
import temperance.hash.StringHash;
import temperance.storage.TpFullText;
import temperance.storage.TpList.TpListResult;
import temperance.util.Lists;

public class MemcachedFullText implements TpFullText {
    
    // TODO: SoftReferenceMap::clean are locked threads
    //protected static final KeyCache<String, Hash> hashKeyCache = new KeyCache<String, Hash>();
    
    protected static final String DEFAULT_ROOT_KEY_PREFIX = TpFullText.class.getSimpleName();
    
    protected static final HashFunction hashFunc = Digest.SHA1;
    
    protected static final String VALUE_KEY_SEPARATOR = "value-hash";
    
    protected final ConnectionPool pool;
    
    protected final MemcachedList list;
    
    public MemcachedFullText(ConnectionPool pool) {
        this.pool = pool;
        this.list = new MemcachedList(pool, DEFAULT_ROOT_KEY_PREFIX);
    }
    
    public long add(final String key, final Hash hash, final String value, final int expire) throws MemcachedOperationException, LockTimeoutException {
        final MemcachedClient client = pool.get();
        try {
            return add(client, key, hash, value, expire);
        } finally {
            pool.release(client);
        }
    }
    
    public List<Long> addAll(final String key, final List<Hash> hashes, final String value, final int expire) throws MemcachedOperationException, LockTimeoutException {
        final MemcachedClient client = pool.get();
        try {
            final List<Long> returnValue = Lists.newArrayList();
            for(Hash hash: hashes){
                long nextHashId = add(client, key, hash, value, expire);
                returnValue.add(Long.valueOf(nextHashId));
            }
            return returnValue;
        } finally {
            pool.release(client);
        }
    }
    
    protected long add(final MemcachedClient client, final String key, final Hash hash, final String value, final int expire) throws MemcachedOperationException, LockTimeoutException {
        final MemcachedStorage storage = client.getStorage();
        final String hashValue = hash.hashValue();
        
        // add hash to key
        list.append(storage, key, hashValue, expire);
        
        // value hash to _key(key, hash(value))
        final Hash valueHash = hashFunc.hash(value);
        list.append(storage, genValueKey(key, valueHash), hashValue, expire);
        
        // add value to _key(key, hash)
        return list.append(storage, genKey(key, hash), value, expire);
    }
    
    public List<Hash> getHashes(final String key, final long offset, final long limit) throws MemcachedOperationException {
        final List<TpListResult> hashResults = getHashesByResult(key, offset, limit);
        final List<Hash> hashList = Lists.newArrayList();
        for(TpListResult result: hashResults){
            hashList.add(new StringHash(result.getValue()));
        }
        return hashList;
    }
    
    public List<Hash> getHashesByValue(final String key, final String value, final long offset, final long limit) throws MemcachedOperationException, LockTimeoutException {
        final Hash valueHash = hashFunc.hash(value);
        final List<TpListResult> results = list.getByResult(genValueKey(key, valueHash), offset, limit);
        final List<Hash> hashList = Lists.newArrayList();
        for(TpListResult result: results){
            hashList.add(new StringHash(result.getValue()));
        }
        return hashList;
    }
    
    public List<TpListResult> getHashesByResult(final String key, final long offset, final long limit) throws MemcachedOperationException {
        return list.getByResult(key, offset, limit);
    }
    
    public List<String> getValues(final String key, final Hash hash, final long offset, final long limit) throws MemcachedOperationException {
        final List<TpListResult> valueResults = getValuesByResult(key, hash, offset, limit);
        final List<String> valueList = Lists.newArrayList();
        for(TpListResult result: valueResults){
            valueList.add(result.getValue());
        }
        return valueList;
    }
    
    public List<TpListResult> getValuesByResult(final String key, final Hash hash, final long offset, final long limit) throws MemcachedOperationException {
        return list.getByResult(genKey(key, hash), offset, limit);
    }
    
    public long hashCount(final String key) throws MemcachedOperationException {
        return list.count(key);
    }
    
    public long hashCountByValue(final String key, final String value) throws MemcachedOperationException {
        final Hash valueHash = hashFunc.hash(value);
        return list.count(genValueKey(key, valueHash));
    }
    
    public long valueCount(final String key, final Hash hash) throws MemcachedOperationException {
        return list.count(genKey(key, hash));
    }
    
    // TODO: all hashes delte
//    public boolean delete(final String key, final int expire) throws MemcachedOperationException, LockTimeoutException {
//        // TODO: fulld elete
//        return sequence.delete(key, expire);
//    }
    
    public boolean deleteByHash(final String key, final Hash hash, final int expire) throws MemcachedOperationException, LockTimeoutException {
        return list.delete(genKey(key, hash), expire);
    }
    
    public boolean deleteByValue(final String key, final String value, final int expire) throws MemcachedOperationException, LockTimeoutException {
        final Hash valueHash = hashFunc.hash(value);
        return list.delete(genValueKey(key, valueHash), expire);
    }
    
    public boolean deleteAtByHash(final String key, final Hash hash, final long index, final int expire) throws MemcachedOperationException, LockTimeoutException {
        return list.deleteAt(genKey(key, hash), index, expire);
    }
    
    public boolean delteAtByValue(final String key, final String value, final long index, final int expire) throws MemcachedOperationException, LockTimeoutException {
        final Hash valueHash = hashFunc.hash(value);
        return list.deleteAt(genValueKey(key, valueHash), index, expire);
    }
    
    // TODO: all hash reindex
//    public void reindex(final String key) throws MemcachedOperationException, LockTimeoutException {
//        sequence.reindex(key);
//    }
    
    public void reindexByHash(final String key, final Hash hash) throws MemcachedOperationException, LockTimeoutException {
        list.reindex(genKey(key, hash));
    }

    protected static String genKey(final String key, final Hash hash){
        return new StringBuffer(key).append(KEY_SEPARATOR).append(hash.hashValue()).toString();
        // TODO: SoftReferenceMap::clean are locked threads
        /*
        synchronized(hashKeyCache){
            String hashKey = hashKeyCache.get(key, hash);
            if(null == hashKey){
                hashKey = new StringBuffer(key).append(KEY_SEPARATOR).append(hash.hashValue()).toString();
                hashKeyCache.put(key, hash, hashKey);
            }
            return hashKey;
        }
        */
    }
    
    protected static String genValueKey(final String key, final Hash hash){
        return new StringBuffer(key)
            .append(KEY_SEPARATOR)
            .append(VALUE_KEY_SEPARATOR)
            .append(KEY_SEPARATOR)
            .append(hashFunc.getAlgorithm())
            .append(KEY_SEPARATOR)
            .append(hash.hashValue())
            .toString();
    }

}
