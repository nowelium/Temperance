package temperance.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import temperance.storage.MemcachedList;
import temperance.util.Lists;

public class ListCommand implements Command {
    
    protected final ThreadPool thread;

    protected final ConnectionPool connection;
    
    public ListCommand(Pooling pooling){
        this.thread = pooling.getThreadPool();
        this.connection = pooling.getConnectionPool();
    }
    
    public Future<List<String>> get(String key, long offset, long limit){
        return submit(new LimitGetValues(connection, key, offset, limit));
    }
    
    public Future<List<String>> getAll(String key){
        return submit(new AllGetValues(connection, key));
    }
    
    public List<Future<List<String>>> getAll(List<String> keys) {
        final List<Future<List<String>>> futures = Lists.newArrayList();
        for(String key: keys){
            futures.add(submit(new AllGetValues(connection, key)));
        }
        return futures;
    }
    
    public void filterAll(String key, Filter filter) throws InterruptedException {
        filterAll(Arrays.asList(key), filter);
    }
    
    public void filterAll(List<String> keys, Filter filter) throws InterruptedException {
        final List<Callable<Void>> tasks = Lists.newArrayList();
        for(String key: keys){
            tasks.add(new AllFilterValues(connection, key, filter));
        }
        invokeAll(tasks);
    }
    
    protected <T> Future<T> submit(Callable<T> task){
        return thread.submit(task);
    }
    
    protected <V> List<Future<V>> invokeAll(List<Callable<V>> tasks) throws InterruptedException {
        return thread.invokeAll(tasks);
    }
    
    public static interface Filter {
        public void execute(List<String> results);
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
            final long count = list.count(key);
            if(count < 1){
                return Collections.emptyList();
            }
            if(count < limit){
                return list.get(key, offset, count);
            }
            
            final List<String> results = Lists.newArrayList();
            for(long i = 0; i < limit; i += SPLIT){
                long splitLimit = SPLIT;
                if(limit < SPLIT){
                    splitLimit = limit;
                }
                results.addAll(list.get(key, offset + i, splitLimit));
            }
            return results;
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
            final long count = list.count(key);
            if(count < 1){
                return Collections.emptyList();
            }
            for(long i = 0; i < count; i += SPLIT){
                long limit = SPLIT;
                if(count < SPLIT){
                    limit = count;
                }
                
                List<String> results = list.get(key, i, limit);
                returnValue.addAll(results);
            }
            return returnValue;
        }
    }
    
    protected static class AllFilterValues implements Callable<Void> {
        private final ConnectionPool pool;
        private final String key;
        private final Filter filter;
        protected AllFilterValues(ConnectionPool pool, String key, Filter filter){
            this.pool = pool;
            this.key = key;
            this.filter = filter;
        }
        public Void call() throws Exception {
            final MemcachedList list = new MemcachedList(pool);
            final long count = list.count(key);
            for(long i = 0; i < count; i += SPLIT){
                long limit = SPLIT;
                if(count < SPLIT){
                    limit = count;
                }
                
                List<String> results = list.get(key, i, limit);
                filter.execute(results);
            }
            return null;
        }
    }

}
