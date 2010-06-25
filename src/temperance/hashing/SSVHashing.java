package temperance.hashing;

import temperance.hash.HashFunction;

public class SSVHashing extends AbstractSeparateValueHashing {

    public SSVHashing(HashFunction function) {
        super(function);
    }

    @Override
    protected String[] split(String str) {
        return str.split("\\s");
    }

}
