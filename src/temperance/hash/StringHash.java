package temperance.hash;

public class StringHash implements Hash {
    
    private final String hash;
    
    public StringHash(String hash){
        this.hash = hash;
    }
    
    public String hashValue(){
        return hash;
    }

}
