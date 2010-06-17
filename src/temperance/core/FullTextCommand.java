package temperance.core;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import temperance.storage.MemcachedFullText;
import temperance.util.Lists;

public class FullTextCommand implements Command {
    
    protected final ThreadPool thread;
    
    protected final ConnectionPool connection;

    public FullTextCommand(Pooling pooling){
        this.thread = pooling.getThreadPool();
        this.connection = pooling.getConnectionPool();
    }
    
    public List<Future<Long>> addAll(final String key, final List<Long> hashes, final String value, final int expire) throws InterruptedException {
        final List<Callable<Long>> tasks = Lists.newArrayList();
        for(Long hash: hashes){
            tasks.add(new AddValue(connection, key, hash, value, expire));
        }
        return invokeAll(tasks);
    }
    
    public Future<List<Long>> getAll(String key){
        return submit(new GetAllHashes(connection, key));
    }

    public Future<List<String>> getAll(String key, Long hash) {
        return submit(new GetAllValue(connection, key, hash));
    }

    public List<Future<List<String>>> getAll(String key, List<Long> hashes) {
        final List<Future<List<String>>> futures = Lists.newArrayList();
        for(Long hash: hashes){
            futures.add(submit(new GetAllValue(connection, key, hash)));
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
        private final int expiration;
        protected AddValue(ConnectionPool pool, String key, Long hash, String value, int expiration){
            this.pool = pool;
            this.key = key;
            this.hash = hash;
            this.value = value;
            this.expiration = expiration;
        }
        public Long call() throws Exception {
            final MemcachedFullText ft = new MemcachedFullText(pool);
            long id = ft.add(key, hash, value, expiration);
            return Long.valueOf(id);
        }
    }
    
    protected static class GetAllHashes implements Callable<List<Long>> {
        private final ConnectionPool pool;
        private final String key;
        protected GetAllHashes(ConnectionPool pool, String key){
            this.pool = pool;
            this.key = key;
        }
        public List<Long> call() throws Exception {
            final MemcachedFullText ft = new MemcachedFullText(pool);
            final List<Long> returnValue = Lists.newArrayList();
            final long targetCount = ft.count(key);
            for(long i = 0; i < targetCount; i += SPLIT){
                List<Long> results = ft.getHash(key, i, SPLIT);
                returnValue.addAll(results);
            }
            return returnValue;
        }
    }

    protected static class GetAllValue implements Callable<List<String>> {
        private final ConnectionPool pool;
        private final String key;
        private final Long hash;
        protected GetAllValue(ConnectionPool pool, String key, Long hash){
            this.pool = pool;
            this.key = key;
            this.hash = hash;
        }
        public List<String> call() throws Exception {
            final MemcachedFullText ft = new MemcachedFullText(pool);
            final List<String> returnValue = Lists.newArrayList();
            final long targetCount = ft.count(key, hash);
            for(long i = 0; i < targetCount; i += SPLIT){
                long splitLimit = SPLIT;
                if(targetCount < SPLIT){
                    splitLimit = targetCount;
                }
                
                List<String> results = ft.get(key, hash, i, splitLimit);
                returnValue.addAll(results);
            }
            return returnValue;
        }
    }
}