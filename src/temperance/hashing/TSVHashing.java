package temperance.hashing;

import temperance.hash.HashFunction;
import temperance.util.StringUtils;

public class TSVHashing extends AbstractSeparateValueHashing {

    public TSVHashing(HashFunction function) {
        super(function);
    }

    @Override
    protected String[] split(String str) {
        return StringUtils.split(str, "\t");
    }

}
