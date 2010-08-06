package temperance.hash;

public class StringHash implements Hash {
    
    private final String hash;
    
    public StringHash(String hash){
        this.hash = hash;
    }
    
    public String hashValue(){
        return hash;
    }
    
    public boolean equals(Object o){
        if(o instanceof Hash){
            Hash h = (Hash) o;
            return h.hashValue().equals(hash);
        }
        return false;
    }

    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder("{");
        buf.append("hash=").append(hash);
        buf.append("}");
        return buf.toString();
    }
}
