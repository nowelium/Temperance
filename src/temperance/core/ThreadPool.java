package temperance.core;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ThreadPool implements LifeCycle {
    
    protected final ExecutorService executor; 
    
    protected final Logger logger = Logger.getLogger(ThreadPool.class.getSimpleName());
    
    public ThreadPool(Configure configure){
        this.executor = new ThreadPoolExecutor(
            configure.getInitialThreadPoolSize(),
            configure.getMaxThreadPoolSize(),
            configure.getThreadKeepAlive(),
            configure.getThreadKeepAliveTimeUnit(),
            new LinkedBlockingQueue<Runnable>()
        ); 
    }
    
    public void init(){
        logger.info("init");
    }
    
    public void destroy(){
        logger.info("destroy");
        
        executor.shutdown();
        try {
            logger.info("await shutdown");
            if(!executor.awaitTermination(10, TimeUnit.SECONDS)){
                executor.shutdownNow();
                
                if(!executor.awaitTermination(10, TimeUnit.SECONDS)){
                    logger.warning("thread pool shutdown failure");
                }
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
