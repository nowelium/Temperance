package temperance.hash;

public class StringHash implements Hash {
    
    private final String hash;
    
    public StringHash(String hash){
        this.hash = hash;
    }
    
    public String hashValue(){
        return hash;
    }

    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder("{");
        buf.append("hash=").append(hash);
        buf.append("}");
        return buf.toString();
    }
}
