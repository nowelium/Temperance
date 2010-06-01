package temperance.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    
    public static <T> IntersectList<T> newIntersectList(){
        return new IntersectList<T>();
    }
    
    public static class IntersectList<T> extends ArrayList<T> {
        private static final long serialVersionUID = 1L;
        
        protected final List<T> values = Lists.newArrayList();

        public boolean intersect(List<T> list){
            //
            // TODO: filter intersect valus
            //
            if(isEmpty()){
                return addAll(list);
            }
            return retainAll(ListUtils.intersect(this, list));
        }
    }

}
