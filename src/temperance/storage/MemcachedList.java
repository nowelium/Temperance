package temperance.storage;

import java.util.ArrayList;
import java.util.List;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.Fetcher;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.MemcachedResult;
import libmemcached.wrapper.MemcachedStorage;
import libmemcached.wrapper.SimpleResult;
import libmemcached.wrapper.type.BehaviorType;
import libmemcached.wrapper.type.ReturnType;

public class MemcachedList {
    
    protected static final String KEY_SEPARATOR = ":";
    
    protected static final int expiration = 0;
    
    protected static final int flag = 0;

    protected final MemcachedClient client;
    
    protected final MemcachedStorage storage;

    protected final String namespace;
    
    public MemcachedList(MemcachedClient client, String namespace){
        this.client = client;
        this.storage = client.getStorage();
        this.namespace = namespace;
        client.getBehavior().set(BehaviorType.SUPPORT_CAS, 1);
    }
    
    public String add(String key, String value) throws LibMemcachedException {
        long nextId = generateId(key);
        client.getStorage().set(valueKey(key, nextId), value, 0, 0);
        return Long.toString(nextId);
    }
    
    public List<String> get(String key, long offset, long limit) throws LibMemcachedException {
        List<String> keys = new ArrayList<String>();
        for(long i = offset, j = 0; i < (offset + limit); ++i, ++j){
            keys.add(valueKey(key, i));
        }
        
        final List<String> returnValue = new ArrayList<String>();
        storage.getMulti(new Fetcher(){
            public void fetch(SimpleResult result) {
                returnValue.add(result.getValue());
            }
        }, keys.toArray(new String[keys.size()]));
        return returnValue;
    }
    
    public long count(String key) throws LibMemcachedException {
        String result = storage.get(incrementKey(key));
        if(null == result){
            return 0L;
        }
        return Long.valueOf(result).longValue();
    }
    
    private long generateId(String key) throws LibMemcachedException {
        String incrementKey = incrementKey(key);
        while(true){
            MemcachedResult result = storage.gets(incrementKey);
            if(null == result){
                storage.set(incrementKey, "1", expiration, flag);
                return 1L;
            }
            
            long increment = Long.valueOf(result.getValue()).longValue() + 1L;
            String incrementValue = Long.toString(increment);
            ReturnType rt = storage.cas(incrementKey, incrementValue, expiration, flag, result.getCAS());
            if(!ReturnType.SUCCESS.equals(rt)){
                continue;
            }
            return increment;
        }
    }
    
    protected String incrementKey(String key){
        return new StringBuilder(namespace)
            .append(KEY_SEPARATOR)
            .append(key)
            .append(".increment").toString();
    }
    
    protected String valueKey(String key, long index){
        return new StringBuilder(namespace)
            .append(KEY_SEPARATOR)
            .append(key)
            .append(KEY_SEPARATOR)
            .append(index).toString();
    }
    
}
