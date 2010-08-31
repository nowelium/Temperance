package temperance.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Pooling implements LifeCycle {
    
    protected final Log logger = LogFactory.getLog(Pooling.class);
    
    protected final Configure configure;
    
    protected final ThreadPool threadPool;
    
    protected final ConnectionPool connectionPool;
    
    //protected final CommandPool commandPool;
    
    public Pooling(Configure configure){
        this.configure = configure;
        this.threadPool = new ThreadPool(configure);
        this.connectionPool = new ConnectionPool(configure);
        //this.commandPool = new CommandPool(configure, this);
    }
    
    public void init(){
        //commandPool.init();
        threadPool.init();
        connectionPool.init();
    }

    public void destroy(){
        //commandPool.init();
        connectionPool.destroy();
        threadPool.destroy();
    }
    
    public ThreadPool getThreadPool(){
        return threadPool;
    }
    
    public ConnectionPool getConnectionPool(){
        return connectionPool;
    }
    
    /*
    public CommandPool getCommandPool(){
        return commandPool;
    }
    */

}
