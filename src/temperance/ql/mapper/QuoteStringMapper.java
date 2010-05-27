package temperance.ql.mapper;

import org.codehaus.jparsec.functors.Map;

public class QuoteStringMapper implements Map<String, String>{
    
    private final char quote;
    
    public QuoteStringMapper(char quote){
        this.quote = quote;
    }
    
    public String map(String str) {
        int index = str.indexOf(quote);
        int lastIndex = str.lastIndexOf(quote);
        if(index < 0 || lastIndex < 0){
            return str;
        }
        return str.substring(index + 1, lastIndex);
    }

}
