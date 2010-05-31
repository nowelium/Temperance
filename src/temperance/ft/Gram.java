package temperance.ft;

import java.util.ArrayList;
import java.util.List;

import temperance.hash.HashFunction;

public class Gram implements Hashing {
    
    protected final HashFunction function;
    
    protected final int split;
    
    public Gram(HashFunction function){
        this(function, 2);
    }
    
    public Gram(HashFunction function, int split){
        this.function = function;
        this.split = split;
    }
    
    public List<Long> parse(String str) {
        List<Long> hashes = new ArrayList<Long>();
        
        int length = str.length();
        for(int i = 0; i < length; ++i){
            int limit = i + split;
            if(length < limit){
                limit = i + 1;
            }
            long hash = function.hash(str.substring(i, limit));
            hashes.add(Long.valueOf(hash));
        }
        return hashes;
    }
}
