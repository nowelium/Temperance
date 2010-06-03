package temperance.ql.mapper;

import org.codehaus.jparsec.functors.Map;

import temperance.ql.FunctionType;

public class FunctionTypeMapper implements Map<String, FunctionType> {
    public FunctionType map(String str) {
        return FunctionType.valueOf(str.toUpperCase());
    }
}
