package temperance.storage;

import java.util.List;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.Fetcher;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.MemcachedResult;
import libmemcached.wrapper.MemcachedStorage;
import libmemcached.wrapper.SimpleResult;
import libmemcached.wrapper.type.BehaviorType;
import libmemcached.wrapper.type.ReturnType;
import temperance.memcached.Pool;
import temperance.util.Lists;

public class MemcachedList {
    
    protected static final String INCREMENT_SUFFIX = ".increment";
    
    protected static final String KEY_SEPARATOR = ":";
    
    protected static final int flag = 0;

    protected final Pool pool;
    
    public MemcachedList(Pool pool){
        this.pool = pool;
    }
    
    public String add(String key, String value, int expire) throws LibMemcachedException {
        long nextId = generateId(key);
        MemcachedClient client = pool.get();
        try {
            client.getStorage().set(indexKey(key, nextId), value, expire, flag);
            return Long.toString(nextId);
        } finally {
            pool.release(client);
        }
    }
    
    public List<String> get(String key, long offset, long limit) throws LibMemcachedException {
        List<String> keys = Lists.newArrayList();
        for(long i = offset, j = 0; i < (offset + limit); ++i, ++j){
            keys.add(indexKey(key, i));
        }
        
        final MemcachedClient client = pool.get();
        try {
            MemcachedStorage storage = client.getStorage();
            
            final List<String> returnValue = Lists.newArrayList();
            storage.getMulti(new Fetcher(){
                public void fetch(SimpleResult result) {
                    returnValue.add(result.getValue());
                }
            }, keys.toArray(new String[keys.size()]));
            return returnValue;
        } finally {
            pool.release(client);
        }
    }
    
    public long count(String key) throws LibMemcachedException {
        final MemcachedClient client = pool.get();
        try {
            MemcachedStorage storage = client.getStorage();
            
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
        client.getBehavior().set(BehaviorType.SUPPORT_CAS, 1);
        try {
            MemcachedStorage storage = client.getStorage();
            
            final String incrementKey = incrementKey(key);
            
            // TODO: infinity loop
            while(true){
                MemcachedResult result = storage.gets(incrementKey);
                if(null == result){
                    storage.set(incrementKey, "1", 0, flag);
                    return 1L;
                }
                
                long increment = Long.valueOf(result.getValue()).longValue() + 1L;
                String incrementValue = Long.toString(increment);
                ReturnType rt = storage.cas(incrementKey, incrementValue, 0, flag, result.getCAS());
                if(!ReturnType.SUCCESS.equals(rt)){
                    continue;
                }
                return increment;
            }
        } finally {
            pool.release(client);
        }
    }
    
    protected String incrementKey(String key){
        return new StringBuilder(key).append(INCREMENT_SUFFIX).toString();
    }
    
    protected String indexKey(String key, long index){
        return new StringBuilder(key).append(KEY_SEPARATOR).append(index).toString();
    }
    
}
