package temperance.handler;

public class Context {
    
    private String memcached;
    
    private String mecabrc;
    
    private String fullTextHashFunction;
    
    public String getMemcached(){
        return memcached;
    }
    
    public void setMemcached(String memcached){
        this.memcached = memcached;
    }
    
    public String getMecabrc(){
        return mecabrc;
    }
    
    public void setMecabrc(String mecabrc){
        this.mecabrc = mecabrc;
    }
    
    public String getFullTextHashFunction(){
        return fullTextHashFunction;
    }
    
    public void setFullTextHashFunction(String fullTextHashFunction){
        this.fullTextHashFunction = fullTextHashFunction;
    }

}
