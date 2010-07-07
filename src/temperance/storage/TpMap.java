package temperance.storage;

import java.util.List;

import temperance.exception.MemcachedOperationException;

public interface TpMap extends TpStorage {
    
    public boolean set(String key, String value, int expire);
    
    public String get(String key) throws MemcachedOperationException;
    
    public List<TpMapResult> getValuesByResult(List<String> keys) throws MemcachedOperationException;
    
    public boolean delete(String key, int expire);
    
    public static class TpMapResult {
        private final String key;
        private final String value;
        public TpMapResult(String key, String value){
            this.key = key;
            this.value = value;
        }
        public String getKey(){
            return key;
        }
        public String getValue(){
            return value;
        }
        public String toString(){
            StringBuilder buf = new StringBuilder("{");
            buf.append("key=").append(key).append(",");
            buf.append("value=").append(value);
            buf.append("}");
            return buf.toString();
        }
    }
}
