package temperance.storage.impl;

import java.util.List;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.Fetcher;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.MemcachedStorage;
import libmemcached.wrapper.SimpleResult;
import libmemcached.wrapper.type.ReturnType;
import temperance.core.ConnectionPool;
import temperance.exception.MemcachedOperationException;
import temperance.storage.TpMap;
import temperance.util.Lists;

public class MemcachedMap implements TpMap {
    
    protected static final String DEFAULT_ROOT_KEY_PREFIX = TpMap.class.getSimpleName();
    
    protected final ConnectionPool pool;
    
    public MemcachedMap(ConnectionPool pool){
        this.pool = pool;
    }
    
    public boolean set(String key, String value, int expire) {
        final MemcachedClient client = pool.get();
        try {
            ReturnType rt = client.getStorage().set(genKey(key), value, expire, DEFAULT_VALUE_FLAG);
            if(ReturnType.BUFFERED.equals(rt) || ReturnType.SUCCESS.equals(rt)){
                return true;
            }
            return false;
        } finally {
            pool.release(client);
        }
    }
    
    public String get(String key) throws MemcachedOperationException {
        final MemcachedClient client = pool.get();
        try {
            return client.getStorage().get(genKey(key));
        } catch(LibMemcachedException e){
            throw new MemcachedOperationException(e);
        } finally {
            pool.release(client);
        }
    }
    
    public List<TpMapResult> getValuesByResult(List<String> keys) throws MemcachedOperationException {
        final MemcachedClient client = pool.get();
        try {
            final List<String> mmcKeys = Lists.newArrayList();
            for(String key: keys){
                mmcKeys.add(genKey(key));
            }
            
            final MemcachedStorage storage = client.getStorage();
            final List<TpMapResult> returnValue = Lists.newArrayList();
            synchronized(storage){
                storage.getMulti(new Fetcher(){
                    public void fetch(SimpleResult result) {
                        returnValue.add(new TpMapResult(result.getKey(), result.getValue()));
                    }
                }, mmcKeys.toArray(new String[mmcKeys.size()]));
                return returnValue;
            }
        } catch(LibMemcachedException e){
            throw new MemcachedOperationException(e);
        } finally {
            pool.release(client);
        }
    }
    
    public boolean delete(String key, int expire) {
        final MemcachedClient client = pool.get();
        try {
            ReturnType rt = client.getStorage().delete(genKey(key), expire);
            if(ReturnType.SUCCESS.equals(rt)){
                return true;
            }
            return false;
        } finally {
            pool.release(client);
        }
    }
    
    protected static String genKey(String key){
        return DEFAULT_ROOT_KEY_PREFIX.concat(KEY_SEPARATOR).concat(key);
    }
}
