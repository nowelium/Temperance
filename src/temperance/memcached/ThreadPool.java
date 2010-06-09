package temperance.memcached;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
    
    private static ThreadPool instance = null;
    
    private final ExecutorService executor = new ThreadPoolExecutor(5, 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    
    private ThreadPool(){
        // nop
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
