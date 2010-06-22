package temperance.core;

public class Pooling implements LifeCycle {
    
    protected final Configure configure;
    
    protected final ThreadPool threadPool;
    
    protected final ConnectionPool connectionPool;
    
    public Pooling(Configure configure){
        this.configure = configure;
        this.threadPool = new ThreadPool(configure);
        this.connectionPool = new ConnectionPool(configure);
    }
    
    public void init(){
        threadPool.init();
        connectionPool.init();
    }

    public void destroy(){
        threadPool.destroy();
        connectionPool.destroy();
    }
    
    public ThreadPool getThreadPool(){
        return threadPool;
    }
    
    public ConnectionPool getConnectionPool(){
        return connectionPool;
    }

}
