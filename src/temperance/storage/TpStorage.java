package temperance.storage;

public interface TpStorage {

    public static final String KEY_SEPARATOR = "$";
    
    public static final int DEFAULT_VALUE_FLAG = 0;
    
    public static interface StreamReader<PARAMETER> {
        public void read(PARAMETER parameter);
    }
    
}
