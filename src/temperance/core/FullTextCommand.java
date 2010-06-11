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
    
    protected <T> Future<List<T>> submit(Callable<List<T>> task){
        return thread.submit(task);
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
                List<String> results = ft.get(key, hash, i, SPLIT);
                returnValue.addAll(results);
            }
            return returnValue;
        }
    }
}
