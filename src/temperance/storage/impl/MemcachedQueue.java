package temperance.storage.impl;

import libmemcached.exception.LibMemcachedException;
import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.MemcachedResult;
import libmemcached.wrapper.MemcachedStorage;
import libmemcached.wrapper.type.ReturnType;
import temperance.core.ConnectionPool;
import temperance.exception.MemcachedOperationException;
import temperance.storage.TpQueue;
import temperance.util.SoftReferenceMap;

public class MemcachedQueue implements TpQueue {
    
    protected static final SoftReferenceMap<String, String> headKeyCache = new SoftReferenceMap<String, String>();
    
    protected static final SoftReferenceMap<String, String> tailKeyCache = new SoftReferenceMap<String, String>();
    
    protected static final String DEFAULT_ROOT_KEY_PREFIX = TpQueue.class.getSimpleName();
    
    protected static final String INITIAL_VALUE = "0";
    
    protected static final int INITIAL_EXPIRE = 0;
    
    protected static final int INITIAL_FLAG = 0;
    
    protected static final String HEAD_SUFFIX = ".head";
    
    protected static final String TAIL_SUFFIX = ".tail";
    
    protected final ConnectionPool pool;
    
    public MemcachedQueue(ConnectionPool pool){
        this.pool = pool;
    }
    
    public boolean enqueue(String key, String value, int expire){
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            final String headKey = headKey(key);
            final String tailKey = tailKey(key);
            
            while(true){
                try {
                    MemcachedResult result = storage.gets(tailKey);
                    if(null == result){
                        storage.set(headKey, INITIAL_VALUE, INITIAL_EXPIRE, INITIAL_FLAG);
                        storage.set(tailKey, INITIAL_VALUE, INITIAL_EXPIRE, INITIAL_FLAG);
                        storage.set(genKey(key, INITIAL_VALUE), value, expire, DEFAULT_VALUE_FLAG);
                        return true;
                    }

                    try {
                        final long increment = Long.valueOf(result.getValue()).longValue() + 1L;
                        String incrementValue = Long.toString(increment);
                        ReturnType rt = storage.cas(
                            tailKey,
                            incrementValue,
                            INITIAL_EXPIRE,
                            INITIAL_FLAG,
                            result.getCAS()
                        );
                        if(!ReturnType.SUCCESS.equals(rt)){
                            continue;
                        }
                        storage.set(
                            genKey(key, incrementValue),
                            value,
                            expire,
                            DEFAULT_VALUE_FLAG
                        );
                        return true;
                    } finally {
                        result.free();
                    }
                } catch(LibMemcachedException e){
                    return false;
                }
            }
        } finally {
            pool.release(client);
        }
    }
    
    public String dequeue(String key) throws MemcachedOperationException {
        final MemcachedClient client = pool.get();
        try {
            final MemcachedStorage storage = client.getStorage();
            
            final String tailKey = tailKey(key);
            final String tailValue = storage.get(tailKey);
            if(null == tailValue){
                return null;
            }

            final String headKey = headKey(key);
            while(true){
                final MemcachedResult head = storage.gets(headKey);
                if(null == head){
                    return null;
                }
                
                try {
                    final String headValue = head.getValue();
                    String valueKey = genKey(key, headValue);
                    String result = storage.get(valueKey);
                    if(null == result){
                        return null;
                    }
                    
                    final long currentId = Long.valueOf(headValue).longValue();
                    final long increment = currentId + 1L;
                    
                    ReturnType rt = storage.cas(
                        headKey,
                        Long.toString(increment),
                        INITIAL_EXPIRE,
                        INITIAL_FLAG,
                        head.getCAS()
                    );
                    if(!ReturnType.SUCCESS.equals(rt)){
                        continue;
                    }
                    storage.delete(valueKey, INITIAL_EXPIRE);
                    return result;
                } finally {
                    head.free();
                }
            }
        } catch(LibMemcachedException e){
            throw new MemcachedOperationException(e);
        } finally {
            pool.release(client);
        }
    }
    
    protected static String genKey(String key, String index){
        StringBuilder buf = new StringBuilder(DEFAULT_ROOT_KEY_PREFIX);
        buf.append(KEY_SEPARATOR);
        buf.append(key);
        buf.append(KEY_SEPARATOR);
        buf.append(index);
        return buf.toString();
    }
    
    protected static String headKey(String key){
        synchronized(headKeyCache){
            String value = headKeyCache.get(key);
            if(null == value){
                StringBuilder buf = new StringBuilder(DEFAULT_ROOT_KEY_PREFIX);
                buf.append(KEY_SEPARATOR);
                buf.append(key);
                buf.append(HEAD_SUFFIX);
                value = buf.toString();
                headKeyCache.put(key, value);
            }
            return value;
        }
    }
    
    protected static String tailKey(String key){
        synchronized(tailKeyCache){
            String value = tailKeyCache.get(key);
            if(null == value){
                StringBuilder buf = new StringBuilder(DEFAULT_ROOT_KEY_PREFIX);
                buf.append(KEY_SEPARATOR);
                buf.append(key);
                buf.append(TAIL_SUFFIX);
                value = buf.toString();
                tailKeyCache.put(key, value);
            }
            return value;
        }
    }
    
}
