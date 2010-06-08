package temperance.ft;

import java.util.List;

import temperance.hash.HashFunction;
import temperance.util.Lists;

public class GramHashing implements Hashing {
    
    protected final HashFunction function;
    
    protected final int split;
    
    public GramHashing(HashFunction function){
        this(function, 2);
    }
    
    public GramHashing(HashFunction function, int split){
        this.function = function;
        this.split = split;
    }
    
    public List<Long> parse(String str) {
        List<Long> hashes = Lists.newArrayList();
        
        int length = str.length();
        for(int i = 0; i < length; ++i){
            int limit = i + split;
            if(length < limit){
                limit = i + 1;
            }
            String substr = str.substring(i, limit);
            if(substr.length() < 2){
                continue;
            }
            
            long hash = function.hash(substr);
            hashes.add(Long.valueOf(hash));
        }
        return hashes;
    }
}
