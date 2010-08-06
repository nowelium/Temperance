package temperance.hash;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import temperance.util.ThreadLocalMap;

public enum Digest implements HashFunction {
    
    MD5("MD5", 7),
    SHA1("SHA1", 7)
    ;
    
    protected static final Charset charset = Charset.forName("UTF-8");
    
    protected static final ThreadLocalMap<String, MessageDigest> digests = new ThreadLocalMap<String, MessageDigest>();
    
    private final String algorithm;
    
    private final int hashSize;
    
    private Digest(String algorithm, int hashSize){
        this.algorithm = algorithm;
        this.hashSize = hashSize;
    }
    
    protected static MessageDigest getInstance(String algorithm) {
        if(digests.containsKey(algorithm)){
            return digests.get(algorithm);
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            digests.put(algorithm, digest);
            return digest;
        } catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }
    
    protected byte[] digest(final String algorithm, final String key){
        return digest(getInstance(algorithm), key);
    }
    
    protected byte[] digest(final MessageDigest digest, final String key){
        synchronized(digest){
            digest.reset();
            digest.update(charset.encode(key));
            return digest.digest();
        }
    }
    
    public String getAlgorithm(){
        return algorithm;
    }
    
    public Hash hash(String key){
        byte[] digest = digest(algorithm, key);
        return new ByteHash(digest, hashSize);
    }
    
}
