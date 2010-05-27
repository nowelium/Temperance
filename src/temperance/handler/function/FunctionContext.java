package temperance.handler.function;

import libmemcached.wrapper.MemcachedClient;

import org.chasen.mecab.wrapper.Tagger;

import temperance.hash.HashFunction;

public class FunctionContext {
    
    private MemcachedClient client;
    
    private Tagger tagger;
    
    private HashFunction hashFunction;
    
    public void setClient(MemcachedClient client){
        this.client = client;
    }
    
    public void setTagger(Tagger tagger){
        this.tagger = tagger;
    }
    
    public void setHashFunction(HashFunction hashFunction){
        this.hashFunction = hashFunction;
    }
    
    public MemcachedClient getClient(){
        return client;
    }
    
    public Tagger getTagger(){
        return tagger;
    }
    
    public HashFunction getHashFunction(){
        return hashFunction;
    }

}
