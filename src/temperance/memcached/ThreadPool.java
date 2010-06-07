package temperance.memcached;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadPool {
    
    private static ThreadPool instance = null;
    
    private final ExecutorService executor;
    
    private ThreadPool(){
        this.executor = Executors.newCachedThreadPool();
    }
    
    public static ThreadPool getInstance(){
        if(null == instance){
            synchronized(ThreadPool.class){
                if(null == instance){
                    instance = new ThreadPool();
                }
            }
        }
        return instance;
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
