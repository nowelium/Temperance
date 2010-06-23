package temperance.core;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import temperance.storage.impl.MemcachedFullText;
import temperance.util.Lists;

public class FullTextCommand implements Command {
    
    protected final ThreadPool thread;
    
    protected final ConnectionPool connection;

    public FullTextCommand(Pooling pooling){
        this.thread = pooling.getThreadPool();
        this.connection = pooling.getConnectionPool();
    }
    
    public List<Future<Long>> addAll(final String key, final List<Long> hashes, final String value, final int expire) {
        final List<Future<Long>> futures = Lists.newArrayList();
        for(Long hash: hashes){
            futures.add(submit(new AddValue(connection, key, hash, value, expire)));
        }
        return futures;
    }
    
    public Future<Boolean> deleteAllValues(final String key, final String value, final int expire) {
        return submit(new DeleteAllValues(connection, key, value, expire));
    }
    
    public Future<List<Long>> getHashes(final String key){
        return submit(new GetHashes(connection, key));
    }

    public List<Future<List<String>>> getValues(final String key, final List<Long> hashes) {
        final List<Future<List<String>>> futures = Lists.newArrayList();
        for(Long hash: hashes){
            futures.add(submit(new GetValues(connection, key, hash)));
        }
        return futures;
    }
    
    protected <T> List<Future<T>> invokeAll(List<Callable<T>> tasks) throws InterruptedException {
        return thread.invokeAll(tasks);
    }
    
    protected <T> Future<T> submit(Callable<T> task){
        return thread.submit(task);
    }
    
    protected static class AddValue implements Callable<Long> {
        private final ConnectionPool pool;
        private final String key;
        private final Long hash;
        private final String value;
        private final int expire;
        protected AddValue(ConnectionPool pool, String key, Long hash, String value, int expire){
            this.pool = pool;
            this.key = key;
            this.hash = hash;
            this.value = value;
            this.expire = expire;
        }
        public Long call() throws Exception {
            final MemcachedFullText ft = new MemcachedFullText(pool);
            final long id = ft.add(key, hash, value, expire);
            return Long.valueOf(id);
        }
    }
    
    protected static class DeleteAllValues implements Callable<Boolean> {
        private final ConnectionPool pool;
        private final String key;
        private final String value;
        private final int expire;
        protected DeleteAllValues(ConnectionPool pool, String key, String value, int expire) {
            this.pool = pool;
            this.key = key;
            this.value = value;
            this.expire = expire;
        }
        public Boolean call() throws Exception {
            final MemcachedFullText ft = new MemcachedFullText(pool);
            final long hashCount = ft.hashCount(key);
            for(long i = 0; i < hashCount; i += SPLIT){
                
            }
            
            final boolean succeed = false;
            return Boolean.valueOf(succeed);
        }
    }
    
    protected static class GetHashes implements Callable<List<Long>> {
        private final ConnectionPool pool;
        private final String key;
        protected GetHashes(ConnectionPool pool, String key){
            this.pool = pool;
            this.key = key;
        }
        public List<Long> call() throws Exception {
            final MemcachedFullText ft = new MemcachedFullText(pool);
            final List<Long> returnValue = Lists.newArrayList();
            final long targetCount = ft.hashCount(key);
            for(long i = 0; i < targetCount; i += SPLIT){
                List<Long> results = ft.getHashes(key, i, SPLIT);
                returnValue.addAll(results);
            }
            return returnValue;
        }
    }

    protected static class GetValues implements Callable<List<String>> {
        private final ConnectionPool pool;
        private final String key;
        private final Long hash;
        protected GetValues(ConnectionPool pool, String key, Long hash){
            this.pool = pool;
            this.key = key;
            this.hash = hash;
        }
        public List<String> call() throws Exception {
            final MemcachedFullText ft = new MemcachedFullText(pool);
            final List<String> returnValue = Lists.newArrayList();
            final long targetCount = ft.valueCount(key, hash);
            for(long i = 0; i < targetCount; i += SPLIT){
                long splitLimit = SPLIT;
                if(targetCount < SPLIT){
                    splitLimit = targetCount;
                }
                
                List<String> results = ft.getValues(key, hash, i, splitLimit);
                returnValue.addAll(results);
            }
            return returnValue;
        }
    }
}
