package temperance.storage;

import libmemcached.wrapper.MemcachedClient;

public class MemcachedFullTextList extends MemcachedList {

    protected final String key;
    
    public MemcachedFullTextList(MemcachedClient client, String key) {
        super(client);
        this.key = key;
    }
    
    protected String incrementKey(String hash){
        return new StringBuilder(key).append(KEY_SEPARATOR).append(hash).append(INCREMENT_SUFFIX).toString();
    }
    
    protected String indexKey(String hash, long index){
        return new StringBuilder(key).append(KEY_SEPARATOR).append(hash).append(KEY_SEPARATOR).append(index).toString();
    }

}
