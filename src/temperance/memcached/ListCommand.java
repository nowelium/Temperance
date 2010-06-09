package temperance.memcached;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import temperance.storage.MemcachedList;
import temperance.util.Lists;

public class ListCommand implements Command {
    
    protected final ThreadPool thread = ThreadPool.getInstance();

    protected final ConnectionPool pool;
    
    public ListCommand(ConnectionPool pool){
        this.pool = pool;
    }
    
    public Future<List<String>> get(String key, long offset, long limit){
        return submit(new LimitGetValues(pool, key, offset, limit));
    }
    
    public Future<List<String>> getAll(String key){
        return submit(new AllGetValues(pool, key));
    }
    
    public List<Future<List<String>>> getAll(List<String> keys) {
        final List<Future<List<String>>> futures = Lists.newArrayList();
        for(String key: keys){
            futures.add(submit(new AllGetValues(pool, key)));
        }
        return futures;
    }
    
    protected Future<List<String>> submit(Callable<List<String>> task){
        return thread.submit(task);
    }
    
    protected static class LimitGetValues implements Callable<List<String>> {
        private final ConnectionPool pool;
        private final String key;
        private final long offset;
        private final long limit;
        protected LimitGetValues(ConnectionPool pool, String key, long offset, long limit){
            this.pool = pool;
            this.key = key;
            this.offset = offset;
            this.limit = limit;
        }
        public List<String> call() throws Exception {
            final MemcachedList list = new MemcachedList(pool);
            if(SPLIT < limit){
                List<String> results = Lists.newArrayList();
                for(long i = 0; i < limit; i += SPLIT){
                    results.addAll(list.get(key, offset + i, SPLIT));
                }
                return results;
            }
            return list.get(key, offset, limit);
        }
    }
    
    protected static class AllGetValues implements Callable<List<String>> {
        private final ConnectionPool pool;
        private final String key;
        protected AllGetValues(ConnectionPool pool, String key){
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
