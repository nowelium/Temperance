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
    
    public long add(String key, String value, int expire) throws LibMemcachedException {
        final MemcachedClient client = pool.get();
        final long nextId = generateId(client, key);
        try {
            client.getStorage().set(indexKey(key, nextId), value, expire, DEFAULT_VALUE_FLAG);
            return nextId;
        } finally {
            pool.release(client);
        }
    }
    
    public List<String> get(String key, long offset, long limit) throws LibMemcachedException {
        final List<String> keys = Lists.newArrayList();
        for(long i = offset; i < (offset + limit); ++i){
            keys.add(indexKey(key, i));
        }
        
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            final List<String> returnValue = Lists.newArrayList();
            
            synchronized(storage){
                storage.getMulti(new Fetcher(){
                    public void fetch(SimpleResult result) {
                        returnValue.add(result.getValue());
                    }
                }, keys.toArray(new String[keys.size()]));
                return returnValue;
            }
        } finally {
            pool.release(client);
        }
    }
    
    public long count(String key) throws LibMemcachedException {
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            
            String result = storage.get(incrementKey(key));
            if(null == result){
                return 0L;
            }
            // result -> 0 are once value, see #generateId
            return Long.valueOf(result).longValue() + 1;
        } finally {
            pool.release(client);
        }
    }
    
    protected long generateId(final MemcachedClient client, final String key) throws LibMemcachedException {
        final MemcachedStorage storage = client.getStorage();
        final String incrementKey = incrementKey(key);
        
        final long begin = System.currentTimeMillis();
        while(true){
            long diff = System.currentTimeMillis() - begin;
            if(LOCK_TIMEOUT < diff){
                throw new MemcachedOperationRuntimeException("incremental lock timeout");
            }
            
            MemcachedResult result = storage.gets(incrementKey);
            if(null == result){
                // start value was 0
                storage.set(incrementKey, "0", INCREMENT_VALUE_EXPIRE, INCREMENT_VALUE_FLAG);
                return 0L;
            }
                
            try {
                final long increment = Long.valueOf(result.getValue()).longValue() + 1L;
                String incrementValue = Long.toString(increment);
                ReturnType rt = storage.cas(incrementKey, incrementValue, INCREMENT_VALUE_EXPIRE, INCREMENT_VALUE_FLAG, result.getCAS());
                if(!ReturnType.SUCCESS.equals(rt)){
                    continue;
                }
                return increment;
            } finally {
                result.free();
            }
        }
    }
    
    protected static String incrementKey(String key){
        synchronized(incrementKeyCache){
            if(incrementKeyCache.containsKey(key)){
                return incrementKeyCache.get(key);
            }
            
            String incrementKey = new StringBuilder(key).append(INCREMENT_SUFFIX).toString();
            incrementKeyCache.put(key, incrementKey);
            return incrementKey;
        }
    }
    
    protected static String indexKey(String key, long index){
        synchronized(indexKeyCache){
            if(indexKeyCache.contains(key, index)){
                return indexKeyCache.get(key, index);
            }
            
            String indexKey = new StringBuilder(key).append(KEY_SEPARATOR).append(index).toString();
            indexKeyCache.put(key, index, indexKey);
            return indexKey;
        }
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
            if(cache.containsKey(key)){
                Map<Long, V> map = cache.get(key);
                return map.put(index, value);
            }
            
            SoftReferenceMap<Long, V> map = new SoftReferenceMap<Long, V>();
            map.put(index, value);
            cache.put(key, map);
            return null;
        }
        
        public V get(String key, Long index){
            if(cache.containsKey(key)){
                Map<Long, V> map = cache.get(key);
                return map.get(index);
            }
            return null;
        }
    }
    
}
