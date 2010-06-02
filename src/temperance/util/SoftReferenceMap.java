package temperance.util;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SoftReferenceMap<K, V> implements Map<K, V> {

    protected final Map<K, SoftReference<V>> map = new HashMap<K, SoftReference<V>>();
    
    public V put(K key, V value){
        SoftReference<V> result = map.put(key, new SoftReference<V>(value));
        if(null != result){
            return result.get();
        }
        return null;
    }
    
    public V get(Object key){
        SoftReference<V> result = map.get(key);
        if(null != result){
            return result.get();
        }
        return null;
    }
    
    public boolean containsKey(Object key){
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        if(!map.containsValue(value)){
            return false;
        }
        Collection<V> collection = values();
        if(null != collection){
            return collection.contains(value);
        }
        return false;
    }

    public void clear() {
        map.clear();
    }
    
    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int size() {
        return map.size();
    }
    
    public V remove(Object key) {
        SoftReference<V> result = map.remove(key);
        if(null != result){
            return result.get();
        }
        return null;
    }
    
    public void putAll(Map<? extends K, ? extends V> t) {
        Iterator<? extends Map.Entry<? extends K, ? extends V>> it = t.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<? extends K, ? extends V> entry = it.next();
            map.put(entry.getKey(), new SoftReference<V>(entry.getValue()));
        }
    }

    public Collection<V> values() {
        Collection<SoftReference<V>> values = map.values();
        List<V> result = Lists.newArrayList();
        // FIXME: clear referent
        for(SoftReference<V> ref: values){
            result.add(ref.get());
        }
        return result;
    }

    public Set<K> keySet() {
        Set<K> set = new HashSet<K>();
        Iterator<Map.Entry<K, SoftReference<V>>> it = map.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<K, SoftReference<V>> entry = it.next();
            K key = entry.getKey();
            set.add(key);
        }
        return set;
    }
    
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> set = new HashSet<Map.Entry<K,V>>();
        Iterator<Map.Entry<K, SoftReference<V>>> it = map.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<K, SoftReference<V>> entry = it.next();
            K key = entry.getKey();
            SoftReference<V> value = entry.getValue();
            if(null == value){
                continue;
            }
            set.add(new Entry<K, V>(key, value.get()));
        }
        return set;
    }
    
    protected static class Entry<K, V> implements Map.Entry<K, V> {

        protected final K key;
        
        protected V value;
        
        protected Entry(K key, V value){
            this.key = key;
            this.value = value;
        }
        
        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            return this.value = value;
        }
    }

}
