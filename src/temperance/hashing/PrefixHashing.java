package temperance.hashing;

import java.util.List;

import temperance.hash.HashFunction;
import temperance.util.Lists;

public class PrefixHashing implements Hashing {
    
    protected final HashFunction function;
    
    public PrefixHashing(HashFunction function){
        this.function = function;
    }
    
    public List<Long> parse(String str) {
        List<Long> hashes = Lists.newArrayList();
        int length = str.length();
        for(int i = 0; i < length; ++i){
            hashes.add(function.hash(str.substring(0, i + 1)));
        }
        return hashes;
    }
}
