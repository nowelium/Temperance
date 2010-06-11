package temperance.function;

import org.chasen.mecab.wrapper.Tagger;

import temperance.core.Pooling;
import temperance.ft.MecabNodeFilter;
import temperance.hash.HashFunction;

public class FunctionContext {
    
    private Pooling pooling;
    
    private Tagger tagger;
    
    private HashFunction hashFunction;
    
    private MecabNodeFilter nodeFilter;
    
    public Pooling getPooling() {
        return pooling;
    }

    public void setPooling(Pooling pooling) {
        this.pooling = pooling;
    }

    public void setTagger(Tagger tagger){
        this.tagger = tagger;
    }

    public Tagger getTagger(){
        return tagger;
    }
    
    public void setHashFunction(HashFunction hashFunction){
        this.hashFunction = hashFunction;
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
