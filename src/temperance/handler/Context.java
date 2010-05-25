package temperance.handler;

public class Context {
    
    protected String host;
    
    protected int port;
    
    public String getHost(){
        return host;
    }
    
    public void setHost(String host){
        this.host = host;
    }
    
    public int getPort(){
        return port;
    }
    
    public void setPort(int port){
        this.port = port;
    }

}
