package temperance.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListUtils {
    
    public static <T> List<T> unique(List<T> list){
        Set<T> set = new HashSet<T>(list);
        list.clear();
        list.addAll(set);
        return list;
    }

}
