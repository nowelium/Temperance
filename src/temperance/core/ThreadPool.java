package temperance.core;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ThreadPool implements LifeCycle {
    
    protected final Configure configure;
    
    protected final ExecutorService executor; 
    
    protected final Log logger = LogFactory.getLog(ThreadPool.class);
    
    public ThreadPool(Configure configure){
        this.configure = configure;
        this.executor = new ThreadPoolExecutor(
            configure.getInitialThreadPoolSize(),
            configure.getMaxThreadPoolSize(),
            configure.getThreadKeepAlive(),
            configure.getThreadKeepAliveTimeUnit(),
            new LinkedBlockingQueue<Runnable>()
        );
    }
    
    public void init(){
        logger.info("init thread pool");
        
        logger.info(new StringBuilder("configure: ")
            .append("initial thread pool size: ").append(configure.getInitialThreadPoolSize())
        );
        logger.info(new StringBuilder("configure: ")
            .append("max thread pool size: ").append(configure.getMaxThreadPoolSize())
        );
        logger.info(new StringBuilder("configure: ")
            .append("thread keep alive: ")
            .append(configure.getThreadKeepAlive()).append(" ").append(configure.getThreadKeepAliveTimeUnit())
        );
    }
    
    public void destroy(){
        logger.info("destroy");
        
        executor.shutdown();
        try {
            logger.info("awaiting termination");
            
            if(!executor.awaitTermination(3600, TimeUnit.SECONDS)){
                if(logger.isWarnEnabled()){
                    logger.warn("awaiting termination fail. shutdwon now");
                }
                
                executor.shutdownNow();
            }
        } catch(InterruptedException e){
            // nop
        } finally {
            logger.info("pool destroyed");
        }
    }
    
    public void execute(Runnable runnable){
        executor.execute(runnable);
    }
    
    public <T> Future<T> submit(Callable<T> task){
        return executor.submit(task);
    }
    
    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks) throws InterruptedException {
        return executor.invokeAll(tasks);
    }

}
