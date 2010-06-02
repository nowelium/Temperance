package temperance.storage;

import java.util.Collections;
import java.util.List;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.Fetcher;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.MemcachedResult;
import libmemcached.wrapper.MemcachedStorage;
import libmemcached.wrapper.SimpleResult;
import libmemcached.wrapper.type.ReturnType;
import temperance.memcached.Pool;
import temperance.util.Lists;

public class MemcachedList {
    
    protected static final String INCREMENT_SUFFIX = ".increment";
    
    protected static final String KEY_SEPARATOR = ":";
    
    protected static final int INCREMENT_VALUE_FLAG = 0;
    
    protected static final int INCREMENT_VALUE_EXPIRE = 0;
    
    protected static final int DEFAULT_VALUE_FLAG = 0;
    
    protected final Pool pool;
    
    public MemcachedList(Pool pool){
        this.pool = pool;
    }
    
    public long add(String key, String value, int expire) throws LibMemcachedException {
        final long nextId = generateId(key);
        MemcachedClient client = pool.get();
        try {
            client.getStorage().set(indexKey(key, nextId), value, expire, DEFAULT_VALUE_FLAG);
            return nextId;
        } finally {
            pool.release(client);
        }
    }
    
    public List<String> get(String key, long offset, long limit) throws LibMemcachedException {
        final List<String> keys = Lists.newArrayList();
        for(long i = offset, j = 0; i < (offset + limit); ++i, ++j){
            keys.add(indexKey(key, i));
        }
        
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            final List<String> returnValue = Lists.newArrayList();
            
            storage.getMulti(new Fetcher(){
                public void fetch(SimpleResult result) {
                    returnValue.add(result.getValue());
                }
            }, keys.toArray(new String[keys.size()]));
            return returnValue;
        } catch(LibMemcachedException e){
            if(ReturnType.NOTFOUND.equals(e.getReturnType())){
                return Collections.emptyList();
            }
            
            throw e;
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
            return Long.valueOf(result).longValue();
        } finally {
            pool.release(client);
        }
    }
    
    private long generateId(String key) throws LibMemcachedException {
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            final String incrementKey = incrementKey(key);
            
            // TODO: infinity loop
            while(true){
                MemcachedResult result = storage.gets(incrementKey);
                if(null == result){
                    storage.set(incrementKey, "1", INCREMENT_VALUE_EXPIRE, INCREMENT_VALUE_FLAG);
                    return 1L;
                }
                
                final long increment = Long.valueOf(result.getValue()).longValue() + 1L;
                String incrementValue = Long.toString(increment);
                ReturnType rt = storage.cas(incrementKey, incrementValue, INCREMENT_VALUE_EXPIRE, INCREMENT_VALUE_FLAG, result.getCAS());
                if(!ReturnType.SUCCESS.equals(rt)){
                    continue;
                }
                return increment;
            }
        } finally {
            pool.release(client);
        }
    }
    
    protected static String incrementKey(String key){
        return new StringBuilder(key).append(INCREMENT_SUFFIX).toString();
    }
    
    protected static String indexKey(String key, long index){
        return new StringBuilder(key).append(KEY_SEPARATOR).append(index).toString();
    }
    
}
