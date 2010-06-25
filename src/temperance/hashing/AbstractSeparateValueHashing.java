package temperance.hashing;

import java.util.List;

import temperance.hash.Hash;
import temperance.hash.HashFunction;
import temperance.util.Lists;
import temperance.util.StringUtils;

public abstract class AbstractSeparateValueHashing implements Hashing {
    
    protected final HashFunction function;
    
    public AbstractSeparateValueHashing(HashFunction function){
        this.function = function;
    }
    
    public List<Hash> parse(String str){
        final List<Hash> hashes = Lists.newArrayList();
        for(String s: split(str)){
            if(StringUtils.isEmpty(s)){
                continue;
            }
            
            hashes.add(function.hash(s));
        }
        return hashes;
    }
    
    protected abstract String[] split(String str);
    
}
