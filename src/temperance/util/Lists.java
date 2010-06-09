package temperance.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Lists {
    
    private Lists(){
        // nop
    }
    
    public static <T> ArrayList<T> newArrayList(){
        return new ArrayList<T>();
    }
    
    public static <T> ArrayList<T> newArrayList(Collection<? extends T> collection){
        return new ArrayList<T>(collection);
    }
    
    public static <T> IntersectalList<T> newIntersectList(){
        return new IntersectalList<T>();
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
    
    public static class IntersectalList<T> extends ArrayList<T> {
        private static final long serialVersionUID = 1L;
        
        protected final List<T> values = Lists.newArrayList();

        public boolean intersect(List<T> list){
            //
            // TODO: filter intersect valus
            //
            if(isEmpty()){
                return addAll(list);
            }
            return retainAll(Lists.intersect(this, list));
        }
    }

}
