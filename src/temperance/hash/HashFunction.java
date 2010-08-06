package temperance.hash;

public interface HashFunction {
    
    public String getAlgorithm();
    
    public Hash hash(String key);

}
