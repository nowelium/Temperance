package temperance.storage.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.Fetcher;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.MemcachedResult;
import libmemcached.wrapper.MemcachedStorage;
import libmemcached.wrapper.SimpleResult;
import libmemcached.wrapper.type.ReturnType;
import temperance.core.ConnectionPool;
import temperance.exception.LockTimeoutException;
import temperance.exception.MemcachedOperationException;
import temperance.exception.MemcachedOperationRuntimeException;
import temperance.storage.TpList;
import temperance.util.Lists;
import temperance.util.SoftReferenceMap;

public class MemcachedList implements TpList {
    
    protected static final SoftReferenceMap<String, String> incrementKeyCache = new SoftReferenceMap<String, String>();
    
    protected static final SoftReferenceMap<String, String> lockKeyCache = new SoftReferenceMap<String, String>();
    
    protected static final KeyCache<String, Long> indexKeyCache = new KeyCache<String, Long>();
    
    protected static final String DEFAULT_ROOT_KEY_PREFIX = TpList.class.getSimpleName();

    protected static final String INCREMENT_SUFFIX = ".increment";
    
    protected static final String LOCK_SUFFIX = ".lock";
    
    protected static final String INITIAL_INCREMENT_VALUE = "0";
    
    protected static final String INITIAL_LOCKED_VALUE = "LOCKED";
    
    protected static final int INCREMENT_VALUE_EXPIRE = 0;
    
    protected static final int INCREMENT_VALUE_FLAG = 0;
    
    protected static final int LOCK_VALUE_EXPIRE = 0;
    
    protected static final int LOCK_VALUE_FLAG = 0;
    
    // TODO: hardcode, runtime parameter
    protected static final long INCREMENT_LOCK_TIMEOUT = TimeUnit.SECONDS.toMillis(3);
    
    // TODO: hardcode, runtime parameter
    protected static final long KEY_LOCK_TIMEOUT = TimeUnit.SECONDS.toMillis(60);
    
    protected final ConnectionPool pool;
    
    protected final String rootKeyPrefix;
    
    public MemcachedList(ConnectionPool pool){
        this(pool, DEFAULT_ROOT_KEY_PREFIX);
    }
    
    protected MemcachedList(ConnectionPool pool, String rootKeyPrefix){
        this.pool = pool;
        this.rootKeyPrefix = rootKeyPrefix;
    }
    
    public long add(final String key, final String value, final int expire) throws MemcachedOperationException, LockTimeoutException {
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            checkLock(storage, key);
            
            return append(storage, key, value, expire);
        } finally {
            pool.release(client);
        }
    }
    
    public List<String> get(final String key, final long offset, final long limit) throws MemcachedOperationException {
        final List<TpListResult> results = getByResult(key, offset, limit);
        final List<String> values = Lists.newArrayList();
        for(TpListResult result: results){
            values.add(result.getValue());
        }
        return values;
    }
    
    public List<TpListResult> getByResult(final String key, final long offset, final long limit) throws MemcachedOperationException {
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            final long count = size(storage, key);
            if(count < 1){
                return Lists.newArrayList();
            }
            
            final KeyList index = new KeyList();
            for(long i = offset; i < (offset + limit); ++i){
                index.add(key, i);
            }
            
            final List<TpListResult> returnValue = Lists.newArrayList();
            synchronized(storage){
                storage.getMultiByKey(new Fetcher(){
                    public void fetch(SimpleResult result) {
                        final long idx = index.getIndex(result.getKey());
                        returnValue.add(new TpListResult(key, idx, result.getValue()));
                    }
                }, key, index.toKeys());
                return returnValue;
            }
        } catch(LibMemcachedException e){
            throw new MemcachedOperationException(e);
        } finally {
            pool.release(client);
        }
    }
    
    public String getAt(final String key, final long index) throws MemcachedOperationException {
        final TpListResult result = getAtByResult(key, index);
        if(null == result){
            return null;
        }
        
        return result.getValue();
    }
    
    public TpListResult getAtByResult(final String key, final long index) throws MemcachedOperationException {
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            return get(storage, key, index);
        } finally {
            pool.release(client);
        }
    }
    
    public long count(final String key) throws MemcachedOperationException {
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            return size(storage, key);
        } finally {
            pool.release(client);
        }
    }
    
    public boolean delete(final String key, final int expire) throws MemcachedOperationException, LockTimeoutException {
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            checkLock(storage, key);
            
            final long limit = size(storage, key);
            for(long i = 0; i < limit; ++i){
                // delete value
                boolean success = remove(storage, key, i, expire);
                if(!success){
                    return false;
                }
            }
            
            ReturnType rt = client.getStorage().delete(incrementKey(key), expire);
            if(ReturnType.SUCCESS.equals(rt) || ReturnType.BUFFERED.equals(rt)){
                return true;
            }
            return false;
        } finally {
            pool.release(client);
        }
    }
    
    public boolean deleteAt(final String key, final long index, final int expire) throws MemcachedOperationException, LockTimeoutException {
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            checkLock(storage, key);
            
            if(remove(storage, key, index, expire)){
                return true;
            }
            return false;
        } finally {
            pool.release(client);
        }
    }
    
    public void reindex(final String key) throws MemcachedOperationException, LockTimeoutException {
        //
        // copying reindex(gc)
        //
        
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            checkLock(storage, key);
            
            boolean locked = lock(storage, key);
            if(!locked){
                throw new MemcachedOperationRuntimeException("try lock failure");
            }
            try {
                final String reIndexKey = "reindex$$" + key;
                
                //
                // pre: cleanup reindex data
                //
                cleanup(storage, reIndexKey, 0);
                
                //
                // {{{ init reindex
                //
                final long limit = size(storage, key);
                for(long i = 0; i < limit; ++i){
                    TpListResult result = get(storage, key, i);
                    if(null == result){
                        continue;
                    }
                    
                    // copy reindex from key
                    // TODO: expire lifetime copy
                    append(storage, reIndexKey, result.getValue(), 0);
                }
                //
                // }}} init reindex
                //
                
                //
                // {{{ reindex
                //
                
                // delete key.increment
                storage.delete(incrementKey(key), 0);
                
                // copy key from reindex
                final long reindexSize = size(storage, reIndexKey);
                for(long i = 0; i < reindexSize; ++i){
                    TpListResult result = get(storage, reIndexKey, i);
                    // set new index
                    // TODO: expire lifetime copy
                    append(storage, key, result.getValue(), 0);
                }
                // delete reindex < limit indexes
                for(long i = reindexSize; i < limit; ++i){
                    remove(storage, key, i, 0);
                }
                
                //
                // }}} reindex
                //
                
                //
                // post: cleanup reindex data
                //
                cleanup(storage, reIndexKey, 0);
            } finally {
                boolean unlocked = unlock(storage, key);
                if(!unlocked){
                    throw new LockTimeoutException("try unlock failure");
                }
            }
        } finally {
            pool.release(client);
        }
    }
    
    protected long append(final MemcachedStorage storage, final String key, final String value, final int expire) throws MemcachedOperationException, LockTimeoutException {
        final long nextId = generateId(storage, key);
        storage.setByKey(key, indexKey(key, nextId), value, expire, DEFAULT_VALUE_FLAG);
        return nextId;
    }
    
    protected boolean remove(final MemcachedStorage storage, final String key, long index, final int expire) throws MemcachedOperationException {
        final String indexKey = indexKey(key, index);
        final ReturnType rt = storage.deleteByKey(key, indexKey, expire);
        if(ReturnType.SUCCESS.equals(rt) || ReturnType.BUFFERED.equals(rt)){
            return true;
        }
        return false;
    }
    
    protected TpListResult get(final MemcachedStorage storage, final String key, final long index) throws MemcachedOperationException {
        try {
            final SimpleResult result = storage.getResultByKey(key, indexKey(key, index));
            if(null == result){
                return null;
            }
            return new TpListResult(key, index, result.getValue());
        } catch(LibMemcachedException e){
            throw new MemcachedOperationException(e);
        }
    }
    
    protected long size(final MemcachedStorage storage, final String key) throws MemcachedOperationException {
        try {
            final String result = storage.getByKey(key, incrementKey(key));
            if(null == result){
                return 0L;
            }
            
            // result -> 0 are once value, see #generateId
            return Long.valueOf(result).longValue() + 1;
        } catch(LibMemcachedException e){
            throw new MemcachedOperationException(e);
        }
    }
    
    protected void cleanup(final MemcachedStorage storage, final String key, final int expire) throws MemcachedOperationException {
        // delete values
        final long cleanupReindexSize = size(storage, key);
        for(long i = 0; i < cleanupReindexSize; ++i){
            remove(storage, key, i, expire);
        }
        // delete key ".increment"
        storage.delete(incrementKey(key), expire);
    }
    
    protected boolean lock(final MemcachedStorage storage, final String key) throws MemcachedOperationException, LockTimeoutException {
        checkLock(storage, key);
        
        final String lockKey = lockKey(key);
        ReturnType rt = storage.setByKey(key, lockKey, INITIAL_LOCKED_VALUE, LOCK_VALUE_EXPIRE, LOCK_VALUE_FLAG);
        return ReturnType.SUCCESS.equals(rt);
    }
    
    protected boolean unlock(final MemcachedStorage storage, final String key) throws MemcachedOperationException {
        final String lockKey = lockKey(key);
        
        try {
            MemcachedResult result = storage.getsByKey(key, lockKey);
            if(null == result){
                return false;
            }
            try {
                if(!INITIAL_LOCKED_VALUE.equals(result.getValue())){
                    return false;
                }
                ReturnType rt = storage.deleteByKey(key, lockKey, LOCK_VALUE_EXPIRE);
                return ReturnType.SUCCESS.equals(rt);
            } finally {
                result.free();
            }
        } catch(LibMemcachedException e){
            throw new MemcachedOperationException(e);
        }
    }
    
    protected long generateId(final MemcachedStorage storage, final String key) throws MemcachedOperationException, LockTimeoutException {
        final String incrementKey = incrementKey(key);
        
        final long begin = System.currentTimeMillis();
        while(true){
            long diff = System.currentTimeMillis() - begin;
            if(INCREMENT_LOCK_TIMEOUT < diff){
                throw new LockTimeoutException("incremental lock timeout");
            }
            
            try {
                MemcachedResult result = storage.getsByKey(key, incrementKey);
                if(null == result){
                    // start value was 0
                    storage.setByKey(key, incrementKey, INITIAL_INCREMENT_VALUE, INCREMENT_VALUE_EXPIRE, INCREMENT_VALUE_FLAG);
                    return 0L;
                }
                
                try {
                    final long increment = Long.valueOf(result.getValue()).longValue() + 1L;
                    String incrementValue = Long.toString(increment);
                    ReturnType rt = storage.casByKey(key, incrementKey, incrementValue, INCREMENT_VALUE_EXPIRE, INCREMENT_VALUE_FLAG, result.getCAS());
                    if(!ReturnType.SUCCESS.equals(rt)){
                        continue;
                    }
                    return increment;
                } finally {
                    result.free();
                }
            } catch(LibMemcachedException e){
                throw new MemcachedOperationException(e);
            }
        }
    }
    
    protected void checkLock(final MemcachedStorage storage, final String key) throws MemcachedOperationException, LockTimeoutException {
        final String lockKey = lockKey(key);
        
        final long begin = System.currentTimeMillis();
        while(true){
            long diff = System.currentTimeMillis() - begin;
            if(KEY_LOCK_TIMEOUT < diff){
                throw new LockTimeoutException("key lock timeout");
            }
            
            try {
                MemcachedResult result = storage.getsByKey(key, lockKey);
                if(null == result){
                    return;
                }
                
                // await unclock
                try {
                    TimeUnit.MILLISECONDS.sleep(1);
                } catch(InterruptedException e){
                    throw new MemcachedOperationRuntimeException(e);
                } finally {
                    result.free();
                }
            } catch(LibMemcachedException e){
                throw new MemcachedOperationException(e);
            }
        }
    }
    
    protected String incrementKey(final String key){
        synchronized(incrementKeyCache){
            String incrementKey = incrementKeyCache.get(key);
            if(null == incrementKey){
                incrementKey = new StringBuilder(rootKeyPrefix).append(KEY_SEPARATOR).append(key).append(INCREMENT_SUFFIX).toString();
                incrementKeyCache.put(key, incrementKey);
            }
            return incrementKey;
        }
    }
    
    protected String lockKey(final String key){
        synchronized(lockKeyCache){
            String lockKey = lockKeyCache.get(key);
            if(null == lockKey){
                lockKey = new StringBuilder(rootKeyPrefix).append(KEY_SEPARATOR).append(key).append(LOCK_SUFFIX).toString();
                lockKeyCache.put(key, lockKey);
            }
            return lockKey;
        }
    }
    
    protected String indexKey(final String key, final long index){
        synchronized(indexKeyCache){
            String indexKey = indexKeyCache.get(key, index);
            if(null == indexKey){
                indexKey = new StringBuilder(rootKeyPrefix).append(KEY_SEPARATOR).append(key).append(KEY_SEPARATOR).append(index).toString();
                indexKeyCache.put(key, index, indexKey);
            }
            return indexKey;
        }
    }
    
    protected class KeyList {
        private final List<String> keys = Lists.newArrayList();
        private final Map<String, Long> indexes = new HashMap<String, Long>();
        
        public void add(String key, long index){
            final String indexKey = indexKey(key, index);
            keys.add(indexKey);
            indexes.put(indexKey, Long.valueOf(index));
        }
        
        public long getIndex(String key){
            return indexes.get(key).longValue();
        }
        
        public String[] toKeys(){
            return keys.toArray(new String[keys.size()]);
        }
    }
    
    protected static class KeyCache<V, IDX> {
        
        protected final SoftReferenceMap<String, Map<IDX, V>> cache = new SoftReferenceMap<String, Map<IDX, V>>();
        
        public boolean contains(String key, IDX index){
            if(!cache.containsKey(key)){
                return false;
            }
            Map<IDX, V> map = cache.get(key);
            if(!map.containsKey(index)){
                return false;
            }
            return true;
        }
        
        public V put(String key, IDX index, V value){
            Map<IDX, V> map = cache.get(key);
            if(null != map){
                return map.put(index, value);
            }
            
            map = new SoftReferenceMap<IDX, V>();
            map.put(index, value);
            cache.put(key, map);
            return null;
        }
        
        public V get(String key, IDX index){
            if(cache.containsKey(key)){
                Map<IDX, V> map = cache.get(key);
                if(null == map){
                    return null;
                }
                return map.get(index);
            }
            return null;
        }
    }
    
}
