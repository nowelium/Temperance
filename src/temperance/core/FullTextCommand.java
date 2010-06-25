package temperance.core;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import temperance.exception.LockTimeoutException;
import temperance.exception.MemcachedOperationException;
import temperance.hash.Hash;
import temperance.storage.TpFullText;
import temperance.storage.TpList.SequenceResult;
import temperance.storage.impl.MemcachedFullText;
import temperance.util.Lists;

public class FullTextCommand implements Command {
    
    protected final ThreadPool thread;
    
    protected final ConnectionPool connection;

    public FullTextCommand(Pooling pooling){
        this.thread = pooling.getThreadPool();
        this.connection = pooling.getConnectionPool();
    }
    
    public List<Future<Long>> addAll(final String key, final List<Hash> hashes, final String value, final int expire) {
        final List<Future<Long>> futures = Lists.newArrayList();
        for(Hash hash: hashes){
            futures.add(submit(new AddValue(connection, key, hash, value, expire)));
        }
        return futures;
    }
    
    public Future<Boolean> deleteAll(final String key, final int expire){
        return submit(new DeleteAll(connection, key, expire));
    }
    
    public Future<Boolean> deleteAllValues(final String key, final int expire, final String value) {
        return submit(new DeleteAllValues(connection, key, expire, value));
    }
    
    public Future<List<Hash>> getHashes(final String key){
        return submit(new GetHashes(connection, key));
    }

    public List<Future<List<String>>> getValues(final String key, final List<Hash> hashes) {
        final List<Future<List<String>>> futures = Lists.newArrayList();
        for(Hash hash: hashes){
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
        private final Hash hash;
        private final String value;
        private final int expire;
        protected AddValue(ConnectionPool pool, String key, Hash hash, String value, int expire){
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
    
    protected static class DeleteAll implements Callable<Boolean> {
        protected final ConnectionPool pool;
        protected final String key;
        protected final int expire;
        protected DeleteAll(ConnectionPool pool, String key, int expire){
            this.pool = pool;
            this.key = key;
            this.expire = expire;
        }
        public Boolean call() throws Exception {
            final TpFullText ft = new MemcachedFullText(pool);
            final long hashCount = ft.hashCount(key);
            try {
                for(long i = 0; i < hashCount; i += SPLIT){
                    List<Hash> hashes = ft.getHashes(key, i, SPLIT);
                    perform(ft, hashes);
                }
                return Boolean.TRUE;
            } catch(MemcachedOperationException e){
                return Boolean.FALSE;
            } catch(LockTimeoutException e){
                return Boolean.FALSE;
            }
        }
        protected void perform(TpFullText ft, List<Hash> hashes) throws MemcachedOperationException, LockTimeoutException {
            for(Hash hash: hashes){
                ft.deleteByHash(key, hash, expire);
            }
        }
    }
    
    protected static class DeleteAllValues extends DeleteAll {
        private final String value;
        protected DeleteAllValues(ConnectionPool pool, String key, int expire, String value) {
            super(pool, key, expire);
            this.value = value;
        }
        protected void perform(TpFullText ft, List<Hash> hashes) throws MemcachedOperationException, LockTimeoutException {
            for(Hash hash: hashes){
                long count = ft.valueCount(key, hash);
                for(int i = 0; i < count; i += SPLIT){
                    List<SequenceResult> results = ft.getValuesByResult(key, hash, i, SPLIT);
                    for(SequenceResult result: results){
                        if(!value.equals(result.getValue())){
                            continue;
                        }
                        ft.deleteAtByHash(key, hash, result.getIndex(), expire);
                    }
                }
            }
        }
    }
    
    protected static class GetHashes implements Callable<List<Hash>> {
        private final ConnectionPool pool;
        private final String key;
        protected GetHashes(ConnectionPool pool, String key){
            this.pool = pool;
            this.key = key;
        }
        public List<Hash> call() throws Exception {
            final TpFullText ft = new MemcachedFullText(pool);
            final List<Hash> returnValue = Lists.newArrayList();
            final long targetCount = ft.hashCount(key);
            for(long i = 0; i < targetCount; i += SPLIT){
                List<Hash> results = ft.getHashes(key, i, SPLIT);
                returnValue.addAll(results);
            }
            return returnValue;
        }
    }

    protected static class GetValues implements Callable<List<String>> {
        private final ConnectionPool pool;
        private final String key;
        private final Hash hash;
        protected GetValues(ConnectionPool pool, String key, Hash hash){
            this.pool = pool;
            this.key = key;
            this.hash = hash;
        }
        public List<String> call() throws Exception {
            final TpFullText ft = new MemcachedFullText(pool);
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
