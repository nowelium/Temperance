package temperance.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListUtils {
    
    private ListUtils(){
        // nop
    }
    
    public static <T> List<T> unique(List<T> list){
        List<T> result = Lists.newArrayList();
        Set<T> set = new HashSet<T>(list);
        result.addAll(set);
        return result;
    }
    
    public static <T> List<T> intersect(List<T> list1, List<T> list2){
        List<T> result = Lists.newArrayList();
        for(T value: list2){
            if(list1.contains(value)){
                result.add(value);
            }
        }
        return result;
    }
    
    public static <T> List<T> subtract(List<T> list1, List<T> list2){
        List<T> result = Lists.newArrayList(list1);
        for(T value: list2){
            result.remove(value);
        }
        return result;
    }
    
}
