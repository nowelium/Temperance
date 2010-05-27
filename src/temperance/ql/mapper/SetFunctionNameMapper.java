package temperance.ql.mapper;

import org.codehaus.jparsec.functors.Map;

import temperance.ql.SetFunction;

public class SetFunctionNameMapper implements Map<String, SetFunction> {
    public SetFunction map(String functionName) {
        return SetFunction.valueOf(functionName);
    }
}
