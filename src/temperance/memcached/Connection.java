package temperance.memcached;

import libmemcached.wrapper.MemcachedClient;

public class Connection {

    protected final MemcachedClient client;
    
    public Connection(MemcachedClient client) {
        this.client = client;
    }
    
    public MemcachedClient get(){
        return client;
    }
    
}
