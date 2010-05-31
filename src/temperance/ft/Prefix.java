package temperance.ft;

import java.util.ArrayList;
import java.util.List;

import temperance.hash.HashFunction;

public class Prefix implements Hashing {
    
    protected final HashFunction function;
    
    public Prefix(HashFunction function){
        this.function = function;
    }
    
    public List<Long> parse(String str) {
        List<Long> hashes = new ArrayList<Long>();
        int length = str.length();
        for(int i = 0; i < length; ++i){
            hashes.add(function.hash(str.substring(0, i + 1)));
        }
        return hashes;
    }
}
