package temperance.hashing;

import java.util.regex.Pattern;

import temperance.hash.HashFunction;

public class SSVHashing extends AbstractSeparateValueHashing {
    
    protected static final Pattern space_pattern = Pattern.compile("[\\s\u3000]");

    public SSVHashing(HashFunction function) {
        super(function);
    }

    @Override
    protected String[] split(String str) {
        return space_pattern.split(str);
    }

}
