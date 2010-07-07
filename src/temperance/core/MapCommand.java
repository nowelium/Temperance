package temperance.core;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import temperance.storage.TpMap;
import temperance.storage.TpMap.TpMapResult;
import temperance.storage.impl.MemcachedMap;
import temperance.util.Lists;

public class MapCommand implements Command {
    
    protected static final Log logger = LogFactory.getLog(MapCommand.class);
    
    protected final ThreadPool thread;
    
    protected final ConnectionPool connection;

    public MapCommand(Pooling pooling){
        this.thread = pooling.getThreadPool();
        this.connection = pooling.getConnectionPool();
    }
    
    public List<TpMapResult> getValues(final List<String> keys) throws InterruptedException, ExecutionException {
        final List<Future<List<TpMapResult>>> tasks = Lists.newArrayList();
        final int size = keys.size();
        for(int i = 0; i < size; i += SPLIT){
            int limit = i + SPLIT;
            if(size < limit){
                limit = size;
            }
            tasks.add(thread.submit(new GetValues(connection, keys.subList(i, limit))));
        }
        
        List<TpMapResult> results = Lists.newArrayList();
        for(Future<List<TpMapResult>> future: tasks){
            results.addAll(future.get());
        }
        return results;
    }
    
    protected class GetValues implements Callable<List<TpMapResult>> {
        protected final ConnectionPool pool;
        protected final List<String> keys;
        protected GetValues(ConnectionPool pool, List<String> keys){
            this.pool = pool;
            this.keys = keys;
        }
        public List<TpMapResult> call() throws Exception {
            final TpMap map = new MemcachedMap(pool);
            return map.getValuesByResult(keys);
        }
    }

}
