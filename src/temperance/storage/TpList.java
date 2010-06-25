package temperance.storage;

import java.util.List;

import temperance.exception.LockTimeoutException;
import temperance.exception.MemcachedOperationException;

public interface TpList {
    
    public long add(String key, String value, int expire) throws MemcachedOperationException, LockTimeoutException;
    
    public List<String> get(String key, long offset, long limit) throws MemcachedOperationException;
    
    public List<TpListResult> getByResult(String key, long offset, long limit) throws MemcachedOperationException;

    public String getAt(String key, long index) throws MemcachedOperationException;
    
    public TpListResult getAtByResult(String key, long index) throws MemcachedOperationException;
    
    public long count(String key) throws MemcachedOperationException;
    
    public boolean delete(String key, int expire) throws MemcachedOperationException, LockTimeoutException;
    
    public boolean deleteAt(String key, long index, int expire) throws MemcachedOperationException, LockTimeoutException;
    
    public void reindex(String key) throws MemcachedOperationException, LockTimeoutException;
    
    public static class TpListResult {
        private final String key;
        private final long index;
        private final String value;
        public TpListResult(String key, long index, String value){
            this.key = key;
            this.index = index;
            this.value = value;
        }
        public String getKey(){
            return key;
        }
        public long getIndex(){
            return index;
        }
        public String getValue(){
            return value;
        }
        public String toString(){
            StringBuilder buf = new StringBuilder("{");
            buf.append("key=").append(key).append(",");
            buf.append("index=").append(index).append(",");
            buf.append("valeu=").append(value);
            buf.append("}");
            return buf.toString();
        }
    }
    
}
