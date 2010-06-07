package temperance.rpc;

import temperance.ft.MecabNodeFilter;
import temperance.hash.HashFunction;

public class Context {
    
    private String memcached;
    
    private int memcachedPoolSize;
    
    private String mecabrc;
    
    private HashFunction fullTextHashFunction;
    
    private MecabNodeFilter nodeFilter;
    
    public String getMemcached(){
        return memcached;
    }
    
    public void setMemcached(String memcached){
        this.memcached = memcached;
    }

    public int getMemcachedPoolSize() {
        return memcachedPoolSize;
    }

    public void setMemcachedPoolSize(int memcachedPoolSize) {
        this.memcachedPoolSize = memcachedPoolSize;
    }
    
    public String getMecabrc(){
        return mecabrc;
    }
    
    public void setMecabrc(String mecabrc){
        this.mecabrc = mecabrc;
    }
    
    public HashFunction getFullTextHashFunction(){
        return fullTextHashFunction;
    }
    
    public void setFullTextHashFunction(HashFunction fullTextHashFunction){
        this.fullTextHashFunction = fullTextHashFunction;
    }

    public MecabNodeFilter getNodeFilter() {
        return nodeFilter;
    }

    public void setNodeFilter(MecabNodeFilter nodeFilter) {
        this.nodeFilter = nodeFilter;
    }

}
