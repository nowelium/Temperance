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
import temperance.memcached.Connection;
import temperance.util.Lists;

public class MemcachedList {
    
    protected static final String INCREMENT_SUFFIX = ".increment";
    
    protected static final String KEY_SEPARATOR = ":";
    
    protected static final int flag = 0;

    protected final Connection connection;
    
    protected final MemcachedClient client;
    
    protected final MemcachedStorage storage;

    public MemcachedList(Connection connection){
        this.connection = connection;
        this.client = connection.get();
        this.storage = client.getStorage();
        client.getBehavior().set(BehaviorType.SUPPORT_CAS, 1);
    }
    
    public String add(String key, String value, int expire) throws LibMemcachedException {
        long nextId = generateId(key);
        client.getStorage().set(indexKey(key, nextId), value, expire, 0);
        return Long.toString(nextId);
    }
    
    public List<String> get(String key, long offset, long limit) throws LibMemcachedException {
        List<String> keys = Lists.newArrayList();
        for(long i = offset, j = 0; i < (offset + limit); ++i, ++j){
            keys.add(indexKey(key, i));
        }
        return get(keys);
    }
    
    public long count(String key) throws LibMemcachedException {
        String result = storage.get(incrementKey(key));
        if(null == result){
            return 0L;
        }
        return Long.valueOf(result).longValue();
    }
    
    protected List<String> get(List<String> keys) throws LibMemcachedException {
        final List<String> returnValue = Lists.newArrayList();
        storage.getMulti(new Fetcher(){
            public void fetch(SimpleResult result) {
                returnValue.add(result.getValue());
            }
        }, keys.toArray(new String[keys.size()]));
        return returnValue;
    }
    
    private long generateId(String key) throws LibMemcachedException {
        final String incrementKey = incrementKey(key);
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
    }
    
    protected String incrementKey(String key){
        return new StringBuilder(key).append(INCREMENT_SUFFIX).toString();
    }
    
    protected String indexKey(String key, long index){
        return new StringBuilder(key).append(KEY_SEPARATOR).append(index).toString();
    }
    
}
