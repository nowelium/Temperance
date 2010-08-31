package temperance.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import temperance.core.command.FullTextCommand;

public class CommandPool implements LifeCycle {
    
    protected static final Log logger = LogFactory.getLog(CommandPool.class);
    
    protected final ExecutorService executor = Executors.newCachedThreadPool();
    
    protected final Configure configure;
    
    // protected final List<Command> commands
    protected final FullTextCommand ft;
    
    public CommandPool(Configure configure, Pooling pooling){
        this.configure = configure;
        this.ft = new FullTextCommand(this, pooling);
    }

    public void init() {
        ft.init();
    }
    
    public void destroy() {
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
    
    public void run(Runnable runnable){
        executor.execute(runnable);
    }
    
    public FullTextCommand fulltext(){
        return ft;
    }
    
}
