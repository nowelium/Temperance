package temperance.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SoftReferenceMap<K, V> implements Map<K, V> {
    
    protected final ReferenceQueue<V> queue = new ReferenceQueue<V>();

    protected final Map<K, SoftReference<V>> cache = new HashMap<K, SoftReference<V>>();
    
    protected final Map<SoftReference<V>, K> refMap = new HashMap<SoftReference<V>, K>();
    
    protected void clean(){
        Reference<? extends V> ref = null;
        while((ref = queue.poll()) != null){
            K key = refMap.remove(ref);
            cache.remove(key);
        }
    }
    
    public V put(K key, V value){
        clean();
        
        SoftReference<V> ref = new SoftReference<V>(value, queue);
        refMap.put(ref, key);
        SoftReference<V> result = cache.put(key, ref);
        if(null != result){
            return result.get();
        }
        return null;
    }
    
    public V get(Object key){
        clean();
        
        SoftReference<V> result = cache.get(key);
        if(null != result){
            return result.get();
        }
        return null;
    }
    
    public boolean containsKey(Object key){
        clean();
        
        return cache.containsKey(key);
    }

    public boolean containsValue(Object value) {
        clean();
        
        if(!cache.containsValue(value)){
            return false;
        }
        Collection<V> collection = values();
        if(null != collection){
            return collection.contains(value);
        }
        return false;
    }

    public void clear() {
        cache.clear();
        refMap.clear();
    }
    
    public boolean isEmpty() {
        return cache.isEmpty();
    }

    public int size() {
        return cache.size();
    }
    
    public V remove(Object key) {
        clean();
        
        SoftReference<V> result = cache.remove(key);
        if(null != result){
            return result.get();
        }
        return null;
    }
    
    public void putAll(Map<? extends K, ? extends V> t) {
        clean();
        
        Iterator<? extends Map.Entry<? extends K, ? extends V>> it = t.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<? extends K, ? extends V> entry = it.next();
            
            SoftReference<V> ref = new SoftReference<V>(entry.getValue(), queue);
            refMap.put(ref, entry.getKey());
            cache.put(entry.getKey(), ref);
        }
    }

    public Collection<V> values() {
        clean();
        
        Collection<SoftReference<V>> values = cache.values();
        List<V> result = Lists.newArrayList();
        // FIXME: clear referent
        for(SoftReference<V> ref: values){
            result.add(ref.get());
        }
        return result;
    }

    public Set<K> keySet() {
        clean();
        
        Set<K> set = new HashSet<K>();
        Iterator<Map.Entry<K, SoftReference<V>>> it = cache.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<K, SoftReference<V>> entry = it.next();
            K key = entry.getKey();
            set.add(key);
        }
        return set;
    }
    
    public Set<Map.Entry<K, V>> entrySet() {
        clean();
        
        Set<Map.Entry<K, V>> set = new HashSet<Map.Entry<K,V>>();
        Iterator<Map.Entry<K, SoftReference<V>>> it = cache.entrySet().iterator();
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
