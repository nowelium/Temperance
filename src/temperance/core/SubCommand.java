package temperance.core;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import temperance.exception.LockTimeoutException;
import temperance.exception.MemcachedOperationException;

abstract class SubCommand<T> implements Callable<T> {
    
    protected Log logger = LogFactory.getLog(getClass());

    public final T call() throws Exception {
        int i = 0;
        while(true){
            i++;
            try {
                return apply();
            } catch(LockTimeoutException e){
                if(10 < i){
                    logger.error(getClass(), e);
                    throw e;
                }
                continue;
            } catch(MemcachedOperationException e){
                logger.error(getClass(), e);
                throw e;
            }
        }
    }
    
    protected abstract T apply() throws LockTimeoutException, MemcachedOperationException;
}
