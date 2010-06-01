package temperance.memcached;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import libmemcached.wrapper.MemcachedClient;

public class Connection extends WeakReference<MemcachedClient> {

    public Connection(MemcachedClient referent) {
        super(referent);
    }
    
    public Connection(MemcachedClient referent, ReferenceQueue<MemcachedClient> queue) {
        super(referent, queue);
    }
    
    @Override
    public MemcachedClient get(){
        return super.get();
    }
    
}
