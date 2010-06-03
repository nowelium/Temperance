package temperance.ql.mapper;

import org.codehaus.jparsec.functors.Map;

import temperance.ql.MengeType;

public class MengeTypeMapper implements Map<String, MengeType> {
    public MengeType map(String functionName) {
        return MengeType.valueOf(functionName.toUpperCase());
    }
}
