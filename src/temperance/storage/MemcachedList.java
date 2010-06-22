package temperance.storage;

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
import temperance.exception.MemcachedOperationRuntimeException;
import temperance.util.Lists;
import temperance.util.SoftReferenceMap;

public class MemcachedList {
    
    protected static final SoftReferenceMap<String, String> incrementKeyCache = new SoftReferenceMap<String, String>();
    
    protected static final KeyCache<String> indexKeyCache = new KeyCache<String>();
    
    protected static final String INCREMENT_SUFFIX = ".increment";
    
    protected static final String KEY_SEPARATOR = "$";
    
    protected static final int INCREMENT_VALUE_FLAG = 0;
    
    protected static final int INCREMENT_VALUE_EXPIRE = 0;
    
    protected static final int DEFAULT_VALUE_FLAG = 0;
    
    // TODO: hardcode, runtime parameter
    protected static final long LOCK_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
    
    protected final ConnectionPool pool;
    
    public MemcachedList(ConnectionPool pool){
        this.pool = pool;
    }
    
    public long add(final String key, final String value, final int expire) throws LibMemcachedException {
        final MemcachedClient client = pool.get();
        try {
            return append(client.getStorage(), key, value, expire);
        } finally {
            pool.release(client);
        }
    }
    
    public List<String> get(final String key, final long offset, final long limit) throws LibMemcachedException {
        final List<String> keys = Lists.newArrayList();
        for(long i = offset; i < (offset + limit); ++i){
            keys.add(indexKey(key, i));
        }
        
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            final List<String> returnValue = Lists.newArrayList();
            
            synchronized(storage){
                storage.getMultiByKey(new Fetcher(){
                    public void fetch(SimpleResult result) {
                        returnValue.add(result.getValue());
                    }
                }, key, keys.toArray(new String[keys.size()]));
                return returnValue;
            }
        } finally {
            pool.release(client);
        }
    }
    
    public long count(final String key) throws LibMemcachedException {
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            
            String result = storage.getByKey(key, incrementKey(key));
            if(null == result){
                return 0L;
            }
            // result -> 0 are once value, see #generateId
            return Long.valueOf(result).longValue() + 1;
        } finally {
            pool.release(client);
        }
    }
    
    public boolean delete(final String key, int expire) throws LibMemcachedException {
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            
            final String incrementKey = incrementKey(key);
            String result = storage.getByKey(key, incrementKey);
            if(null != result){
                long limit = Long.valueOf(result).longValue();
                for(long i = 0; i < limit; ++i){
                    // delete value
                    boolean success = deleteAt(storage, key, i, expire);
                    if(!success){
                        return false;
                    }
                }
            }
            
            ReturnType rt = client.getStorage().delete(incrementKey, expire);
            if(ReturnType.DELETED.equals(rt) || ReturnType.BUFFERED.equals(rt)){
                return true;
            }
            return false;
        } finally {
            pool.release(client);
        }
    }
    
    public boolean delete(final String key, final long index, final int expire) throws LibMemcachedException {
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            
            if(deleteAt(storage, key, index, expire)){
                //
                // TODO: reindex
                //
                
                return true;
            }
            return false;
        } finally {
            pool.release(client);
        }
    }
    
    protected static long append(final MemcachedStorage storage, final String key, final String value, final int expire) throws LibMemcachedException {
        final long nextId = generateId(storage, key);
        storage.setByKey(key, indexKey(key, nextId), value, expire, DEFAULT_VALUE_FLAG);
        return nextId;
    }
    
    protected static boolean deleteAt(final MemcachedStorage storage, final String key, long index, final int expire) throws LibMemcachedException {
        final String indexKey = indexKey(key, index);
        ReturnType rt = storage.deleteByKey(key, indexKey, expire);
        if(ReturnType.DELETED.equals(rt) || ReturnType.BUFFERED.equals(rt)){
            return true;
        }
        return false;
    }
    
    protected static long generateId(final MemcachedStorage storage, final String key) throws LibMemcachedException {
        final String incrementKey = incrementKey(key);
        
        final long begin = System.currentTimeMillis();
        while(true){
            long diff = System.currentTimeMillis() - begin;
            if(LOCK_TIMEOUT < diff){
                throw new MemcachedOperationRuntimeException("incremental lock timeout");
            }
            
            MemcachedResult result = storage.getsByKey(key, incrementKey);
            if(null == result){
                // start value was 0
                storage.setByKey(key, incrementKey, "0", INCREMENT_VALUE_EXPIRE, INCREMENT_VALUE_FLAG);
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
        }
    }
    
    protected static String incrementKey(final String key){
        String incrementKey = incrementKeyCache.get(key);
        if(null == incrementKey){
            incrementKey = new StringBuilder(key).append(INCREMENT_SUFFIX).toString();
            incrementKeyCache.put(key, incrementKey);
        }
        return incrementKey;
    }
    
    protected static String indexKey(final String key, final long index){
        String indexKey = indexKeyCache.get(key, index);
        if(null == indexKey){
            indexKey = new StringBuilder(key).append(KEY_SEPARATOR).append(index).toString();
            indexKeyCache.put(key, index, indexKey);
        }
        return indexKey;
    }
    
    protected static class KeyCache<V> {
        protected final SoftReferenceMap<String, Map<Long, V>> cache = new SoftReferenceMap<String, Map<Long, V>>();
        
        public boolean contains(String key, Long index){
            if(!cache.containsKey(key)){
                return false;
            }
            Map<Long, V> map = cache.get(key);
            if(!map.containsKey(index)){
                return false;
            }
            return true;
        }
        
        public V put(String key, Long index, V value){
            Map<Long, V> map = cache.get(key);
            if(null != map){
                return map.put(index, value);
            }
            
            map = new SoftReferenceMap<Long, V>();
            map.put(index, value);
            cache.put(key, map);
            return null;
        }
        
        public V get(String key, Long index){
            if(cache.containsKey(key)){
                Map<Long, V> map = cache.get(key);
                if(null == map){
                    return null;
                }
                return map.get(index);
            }
            return null;
        }
    }
    
}
