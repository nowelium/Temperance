package temperance.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ThreadPool implements LifeCycle {
    
    protected static final Log logger = LogFactory.getLog(ThreadPool.class);

    protected final Configure configure;
    
    protected final LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
    
    protected final ThreadFactory factory = new ThreadFactoryImpl();
    
    protected final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor(factory);

    protected final TrackingThreadPoolExecutor executor; 
    
    public ThreadPool(Configure configure){
        this.configure = configure;
        this.executor = new TrackingThreadPoolExecutor(
            configure.getInitialThreadPoolSize(),
            configure.getMaxThreadPoolSize(),
            configure.getThreadKeepAlive(),
            configure.getThreadKeepAliveTimeUnit(),
            workQueue,
            factory
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
        
        if(configure.isVerboseThread()){
            scheduledExecutor.scheduleWithFixedDelay(new ThreadMonitor(), 60, 60, TimeUnit.SECONDS);
        }
    }
    
    public void destroy(){
        logger.info("destroy");
        
        logger.info("scheduler shutdown");
        scheduledExecutor.shutdown();
        try {
            logger.info("awaiting scheduler termination...");
            if(!scheduledExecutor.awaitTermination(60, TimeUnit.SECONDS)){
                if(logger.isWarnEnabled()){
                    logger.warn("scheduler awaiting termination fail. shutdown now");
                }
                
                scheduledExecutor.shutdownNow();
            }
        } catch(InterruptedException e){
            // nop
        } finally {
            logger.info("scheduler destroyed");
        }
        
        logger.info("pool shutdown");
        executor.shutdown();
        try {
            logger.info("awaiting termination...");
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
    
    public Set<Runnable> getInProgressTasks(){
        return executor.getInProgressTasks();
    }
    
    public Collection<Runnable> getQueuedTasks(){
        return executor.getQueuedTasks();
    }
    
    public long getTotalTasks(){
        return executor.getTotalTasks();
    }
    
    public long getTotalTime(){
        return executor.getTotalTime();
    }
    
    protected class ThreadMonitor implements Runnable {
        public void run(){
            final int inProgress = executor.getInProgressTasks().size();
            final int queued = executor.getQueuedTasks().size();
            final long total = executor.getTotalTasks();
            final long count = inProgress + queued + total;
            logger.info(new StringBuilder("thread monitor {")
                .append("in-progress:").append(inProgress).append(",")
                .append("queued:").append(queued).append(",")
                .append("total:").append(total).append(",")
                .append("count:").append(count)
                .append("}")
            );
        }
    }
    
    protected static class TrackingThreadPoolExecutor extends ThreadPoolExecutor {
        
        protected final Map<Runnable, Boolean> inProgress = Collections.synchronizedMap(new WeakHashMap<Runnable, Boolean>());
        
        protected final ThreadLocal<Long> startTime = new ThreadLocal<Long>();
        
        protected final AtomicLong totalTime = new AtomicLong(0);
        
        protected final AtomicLong totalTasks = new AtomicLong(0);

        public TrackingThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                long keepAliveTime, TimeUnit unit,
                BlockingQueue<Runnable> workQueue,
                RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
        }

        public TrackingThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                long keepAliveTime, TimeUnit unit,
                BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
                RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        }

        public TrackingThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                long keepAliveTime, TimeUnit unit,
                BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        }

        public TrackingThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                long keepAliveTime, TimeUnit unit,
                BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }
        
        protected void beforeExecute(Thread th, Runnable command) {
            super.beforeExecute(th, command);
            
            inProgress.put(command, Boolean.TRUE);
            startTime.set(Long.valueOf(System.currentTimeMillis()));
        }
        
        protected void afterExecute(Runnable command, Throwable t) {
            long elapsed = System.currentTimeMillis() - startTime.get().longValue();
            totalTime.addAndGet(elapsed);
            totalTasks.incrementAndGet();

            inProgress.remove(command);
            super.afterExecute(command, t);
        }
        
        public Set<Runnable> getInProgressTasks() {
            return Collections.unmodifiableSet(inProgress.keySet());
        }
        
        public Collection<Runnable> getQueuedTasks(){
            return Collections.unmodifiableCollection(super.getQueue());
        }
        
        public long getTotalTasks(){
            return totalTasks.get();
        }
        
        public long getTotalTime(){
            return totalTime.get();
        }
    }
    
    protected static class ThreadFactoryImpl implements ThreadFactory {
        
        protected static final AtomicInteger poolNumber = new AtomicInteger(1);
        
        protected final ThreadGroup group;
        
        protected final AtomicInteger threadNumber = new AtomicInteger(1);
        
        protected final String namePrefix;
        
        private final boolean daemon;
        
        public ThreadFactoryImpl(){
            this(false);
        }
        
        public ThreadFactoryImpl(boolean daemon) {
            this.daemon = daemon;
            SecurityManager securityManager = System.getSecurityManager();
            if(null == securityManager){
                this.group = Thread.currentThread().getThreadGroup();
            } else {
                this.group = securityManager.getThreadGroup();
            }
            
            StringBuilder buf = new StringBuilder("tp-pool-").append(poolNumber.getAndIncrement());
            buf.append("-");
            if(daemon){
                buf.append("daemon-");
            }
            buf.append("thread-");
            this.namePrefix = buf.toString();
        }
        
        public Thread newThread(Runnable runnable) {
            StringBuilder buf = new StringBuilder(namePrefix);
            buf.append(threadNumber.getAndIncrement());
            
            Thread th = new Thread(group, runnable, buf.toString(), 0L);
            if(daemon){
                th.setDaemon(daemon);
            }
            if(th.getPriority() != Thread.NORM_PRIORITY){
                th.setPriority(Thread.NORM_PRIORITY);
            }
            return th;
        }
    }

}
