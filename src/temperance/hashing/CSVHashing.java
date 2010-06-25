package temperance.hashing;

import temperance.hash.HashFunction;
import temperance.util.StringUtils;

public class CSVHashing extends AbstractSeparateValueHashing {

    public CSVHashing(HashFunction function) {
        super(function);
    }

    @Override
    protected String[] split(String str) {
        return StringUtils.split(str, ",");
    }

}
