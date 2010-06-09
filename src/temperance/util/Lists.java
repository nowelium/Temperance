package temperance.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

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
    
    /**
     * TODO: safe order
     */
    public static <T> List<T> unique(List<T> list){
        List<T> result = Lists.newArrayList();
        Set<T> set = new HashSet<T>(list);
        result.addAll(set);
        return result;
    }
    
    public static <T> List<T> intersect(List<T> list1, List<T> list2){
        List<T> result = Lists.newArrayList();
        for(T value: list1){
            if(list2.contains(value)){
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
    
    public static class IntersectalList<T> {
        
        protected final AtomicReference<List<T>> ref = new AtomicReference<List<T>>(new ArrayList<T>());

        public void intersect(List<T> list){
            //
            // TODO: filter intersect valus
            //
            List<T> values = ref.get();
            if(values.isEmpty()){
                ref.set(list);
                return;
            }
            
            List<T> intersect = Lists.intersect(values, list);
            values.retainAll(intersect);
            ref.set(values);
        }
        public List<T> getValues(){
            return ref.get();
        }
    }

}
