package temperance.handler.function;

import org.chasen.mecab.wrapper.Tagger;

import temperance.ft.MecabNodeFilter;
import temperance.hash.HashFunction;
import temperance.memcached.Pool;

public class FunctionContext {
    
    private Pool pool;
    
    private Tagger tagger;
    
    private HashFunction hashFunction;
    
    private MecabNodeFilter nodeFilter;
    
    public void setPool(Pool pool){
        this.pool = pool;
    }
    
    public void setTagger(Tagger tagger){
        this.tagger = tagger;
    }
    
    public void setHashFunction(HashFunction hashFunction){
        this.hashFunction = hashFunction;
    }
    
    public Pool getPool(){
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
