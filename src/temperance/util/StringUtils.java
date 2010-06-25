package temperance.util;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class StringUtils {
    
    protected static final String EMPTY_STRING = "";
    
    protected static final String[] EMPTY_STRINGS = new String[0];
    
    private StringUtils(){
        // nop
    }
    
    public static String[] split(String source, String delimiter){
        if(null == source){
            return EMPTY_STRINGS;
        }
        if(source.length() < 1){
            return EMPTY_STRINGS;
        }
        
        List<String> items = Lists.newArrayList();
        StringTokenizer tokenizer = new StringTokenizer(source, delimiter);
        while(tokenizer.hasMoreTokens()){
            items.add(tokenizer.nextToken());
        }
        return items.toArray(new String[items.size()]);
    }
    
    public static String join(List<String> items, char separator){
        StringBuilder buf = new StringBuilder();
        Iterator<String> it = items.iterator();
        while(it.hasNext()){
            buf.append(it.next());
            if(it.hasNext()){
                buf.append(separator);
            }
        }
        return buf.toString();
    }

}
