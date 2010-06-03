package temperance.memcached;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import temperance.storage.MemcachedList;
import temperance.util.Lists;

public class ListCommand implements ConcurrentCommand {
    
    protected final ExecutorService service;

    protected final Pool pool;
    
    public ListCommand(Pool pool){
        this.service = pool.sharedThreadPool;
        this.pool = pool;
    }
    
    public Future<List<String>> getAll(String key){
        return submit(new GetAllValues(pool, key));
    }
    
    public List<Future<List<String>>> getAll(List<String> keys) {
        final List<Future<List<String>>> futures = Lists.newArrayList();
        for(String key: keys){
            futures.add(submit(new GetAllValues(pool, key)));
        }
        return futures;
    }
    
    protected Future<List<String>> submit(Callable<List<String>> task){
        return service.submit(task);
    }
    
    protected static class GetAllValues implements Callable<List<String>> {
        private final Pool pool;
        private final String key;
        protected GetAllValues(Pool pool, String key){
            this.pool = pool;
            this.key = key;
        }
        public List<String> call() throws Exception {
            final MemcachedList list = new MemcachedList(pool);
            final List<String> returnValue = Lists.newArrayList();
            long targetCount = list.count(key);
            for(long i = 0; i < targetCount; i += SPLIT){
                List<String> results = list.get(key, i, SPLIT);
                returnValue.addAll(results);
            }
            return returnValue;
        }
    }

}
