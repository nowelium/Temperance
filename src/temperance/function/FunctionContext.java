package temperance.function;

import org.chasen.mecab.wrapper.Tagger;

import temperance.ft.MecabNodeFilter;
import temperance.hash.HashFunction;
import temperance.memcached.ConnectionPool;

public class FunctionContext {
    
    private ConnectionPool pool;
    
    private Tagger tagger;
    
    private HashFunction hashFunction;
    
    private MecabNodeFilter nodeFilter;
    
    public void setPool(ConnectionPool pool){
        this.pool = pool;
    }
    
    public void setTagger(Tagger tagger){
        this.tagger = tagger;
    }
    
    public void setHashFunction(HashFunction hashFunction){
        this.hashFunction = hashFunction;
    }
    
    public ConnectionPool getPool(){
        return pool;
    }
    
    public Tagger getTagger(){
        return tagger;
    }
    
    public HashFunction getHashFunction(){
        return hashFunction;
    }

    public MecabNodeFilter getNodeFilter() {
        return nodeFilter;
    }

    public void setNodeFilter(MecabNodeFilter nodeFilter) {
        this.nodeFilter = nodeFilter;
    }

}
