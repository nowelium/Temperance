package temperance.ql.mapper;

import org.codehaus.jparsec.functors.Map;

public class SingleQuoteStringMapper implements Map<String, String>{
    public String map(String str) {
        return str.substring(1, str.lastIndexOf('\''));
    }

}
