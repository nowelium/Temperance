package temperance.core;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import temperance.exception.LockTimeoutException;
import temperance.exception.MemcachedOperationException;
import temperance.hash.Hash;
import temperance.storage.TpStorage.StreamReader;
import temperance.storage.TpFullText;
import temperance.storage.TpList.TpListResult;
import temperance.storage.impl.MemcachedFullText;
import temperance.util.Lists;

public class FullTextCommand extends Command {
    
    protected static final Log logger = LogFactory.getLog(FullTextCommand.class);
    
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
    
    public Future<Boolean> reindex(final String key) {
        return submit(new ReindexAllHashes(connection, key));
    }
    
    protected <T> List<Future<T>> invokeAll(List<Callable<T>> tasks) throws InterruptedException {
        return thread.invokeAll(tasks);
    }
    
    protected <T> Future<T> submit(Callable<T> task){
        return thread.submit(task);
    }
    
    protected static class AddValue extends SubCommand<Long> {
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
        public Long apply() throws LockTimeoutException, MemcachedOperationException {
            final MemcachedFullText ft = new MemcachedFullText(pool);
            final long id = ft.add(key, hash, value, expire);
            return Long.valueOf(id);
        }
    }
    
    protected static class DeleteAll extends SubCommand<Boolean> {
        protected static final Log logger = LogFactory.getLog(FullTextCommand.class);
        protected final ConnectionPool pool;
        protected final String key;
        protected final int expire;
        protected DeleteAll(ConnectionPool pool, String key, int expire){
            this.pool = pool;
            this.key = key;
            this.expire = expire;
        }
        public Boolean apply() throws LockTimeoutException, MemcachedOperationException {
            final TpFullText ft = new MemcachedFullText(pool);
            final long hashCount = ft.hashCount(key);
            try {
                final performHash performHash = new performHash();
                for(long i = 0; i < hashCount; i += SPLIT){
                    long limit = SPLIT;
                    // getHashes(offset, limit) -> valueOf(SPLIT) not exceed valueOf(hashCount): thx messy
                    if(hashCount <= (i + SPLIT)){
                        limit = hashCount - i;
                    }
                    ft.getHashes(key, i, limit, performHash);
                }
                return Boolean.TRUE;
            } catch(MemcachedOperationException e){
                if(logger.isErrorEnabled()){
                    logger.error(DeleteAll.class, e);
                }
                return Boolean.FALSE;
            }
        }
        private class performHash implements StreamReader<Hash> {
            private final Log logger = LogFactory.getLog(performHash.class);
            private final TpFullText ft = new MemcachedFullText(pool);
            
            @Override
            public void read(Hash hash) {
                try {
                    ft.deleteByHash(key, hash, expire);
                } catch (MemcachedOperationException e) {
                    if(logger.isErrorEnabled()){
                        logger.error(performHash.class, e);
                    }
                } catch (LockTimeoutException e) {
                    if(logger.isErrorEnabled()){
                        logger.error(performHash.class, e);
                    }
                }
            }
        }
    }
    
    protected static class DeleteAllValues extends SubCommand<Boolean> {
        protected final ConnectionPool pool;
        protected final String key;
        protected final int expire;
        protected final String value;
        protected DeleteAllValues(ConnectionPool pool, String key, int expire, String value) {
            this.pool = pool;
            this.key = key;
            this.expire = expire;
            this.value = value;
        }
        public Boolean apply() throws LockTimeoutException, MemcachedOperationException {
            final TpFullText ft = new MemcachedFullText(pool);
            final long hashCount = ft.hashCountByValue(key, value);
            try {
                final performHash performHash = new performHash();
                // first. delete hash by value
                for(long i = 0; i < hashCount; i += SPLIT){
                    long limit = SPLIT;
                    // get(offset, limit) -> valueOf(SPLIT) not exceed valueOf(hashCount): thx messy
                    if(hashCount <= (i + SPLIT)){
                        limit = hashCount - i;
                    }
                    ft.getHashesByValue(key, value, i, limit, performHash);
                }
                // second. delete value
                ft.deleteByValue(key, value, expire);
                
                // TODO: third. delete hash!! 
                return Boolean.TRUE;
            } catch(MemcachedOperationException e){
                if(logger.isErrorEnabled()){
                    logger.error(DeleteAllValues.class, e);
                }
                return Boolean.FALSE;
            } catch(LockTimeoutException e){
                if(logger.isErrorEnabled()){
                    logger.error(DeleteAllValues.class, e);
                }
                return Boolean.FALSE;
            }
        }
        private class performHash implements StreamReader<Hash> {
            private final Log logger = LogFactory.getLog(performHash.class);
            private final TpFullText ft = new MemcachedFullText(pool);
            public void read(Hash hash){
                try {
                    final long valueCount = ft.valueCount(key, hash);
                    final performValue performValue = new performValue(hash);
                    for(long i = 0; i < valueCount; i += SPLIT){
                        long limit = SPLIT;
                        // get(offset, limit) -> valueOf(SPLIT) not exceed valueOf(limit): thx messy
                        if(valueCount <= (i + SPLIT)){
                            limit = valueCount - i;
                        }
                        ft.getValuesByResult(key, hash, i, limit, performValue);
                    }
                } catch(MemcachedOperationException e){
                    if(logger.isErrorEnabled()){
                        logger.error(performHash.class, e);
                    }
                }
            }
        }
        private class performValue implements StreamReader<TpListResult> {
            private final Log logger = LogFactory.getLog(performValue.class);
            private final Hash hash;
            private final TpFullText ft = new MemcachedFullText(pool);
            private performValue(Hash hash){
                this.hash = hash;
            }
            public void read(TpListResult result){
                try {
                    if(value.equals(result.getValue())){
                        ft.deleteAtByHash(key, hash, result.getIndex(), expire);
                    }
                } catch(MemcachedOperationException e){
                    if(logger.isErrorEnabled()){
                        logger.error(performValue.class, e);
                    }
                } catch(LockTimeoutException e){
                    if(logger.isErrorEnabled()){
                        logger.error(performValue.class, e);
                    }
                }
            }
        }
    }
    
    protected static class GetHashes extends SubCommand<List<Hash>> {
        private final ConnectionPool pool;
        private final String key;
        protected GetHashes(ConnectionPool pool, String key){
            this.pool = pool;
            this.key = key;
        }
        public List<Hash> apply() throws LockTimeoutException, MemcachedOperationException {
            final TpFullText ft = new MemcachedFullText(pool);
            final List<Hash> returnValue = Lists.newArrayList();
            final long targetCount = ft.hashCount(key);
            for(long i = 0; i < targetCount; i += SPLIT){
                long limit = SPLIT;
                // get(offset, limit) -> valueOf(SPLIT) not exceed valueOf(limit): thx messy
                if(targetCount <= (i + SPLIT)){
                    limit = targetCount - i;
                }
                List<Hash> results = ft.getHashes(key, i, limit);
                returnValue.addAll(results);
            }
            return returnValue;
        }
    }

    protected static class GetValues extends SubCommand<List<String>> {
        private final ConnectionPool pool;
        private final String key;
        private final Hash hash;
        protected GetValues(ConnectionPool pool, String key, Hash hash){
            this.pool = pool;
            this.key = key;
            this.hash = hash;
        }
        public List<String> apply() throws LockTimeoutException, MemcachedOperationException {
            final TpFullText ft = new MemcachedFullText(pool);
            final List<String> returnValue = Lists.newArrayList();
            final long targetCount = ft.valueCount(key, hash);
            for(long i = 0; i < targetCount; i += SPLIT){
                long limit = SPLIT;
                // get(offset, limit) -> valueOf(SPLIT) not exceed valueOf(limit): thx messy
                if(targetCount <= (i + SPLIT)){
                    limit = targetCount - i;
                }
                
                List<String> results = ft.getValues(key, hash, i, limit);
                returnValue.addAll(results);
            }
            return returnValue;
        }
    }
    
    protected static class ReindexAllHashes extends SubCommand<Boolean> {
        private final ConnectionPool pool;
        private final String key;
        protected ReindexAllHashes(ConnectionPool pool, String key){
            this.pool = pool;
            this.key = key;
        }
        public Boolean apply() throws LockTimeoutException, MemcachedOperationException {
            try {
                // TODO: logic
                final TpFullText ft = new MemcachedFullText(pool);
                final long hashCount = ft.hashCount(key);
                for(long i = 0; i < hashCount; i += SPLIT){
                    long limit = SPLIT;
                    // get(offset, limit) -> valueOf(SPLIT) not exceed valueOf(limit): thx messy
                    if(hashCount <= (i + SPLIT)){
                        limit = hashCount - i;
                    }
                    
                    List<Hash> hashes = ft.getHashes(key, i, limit);
                    for(Hash hash: hashes){
                        ft.reindexByHash(key, hash);
                    }
                }
                
                return Boolean.TRUE;
            } catch(MemcachedOperationException e){
                if(logger.isErrorEnabled()){
                    logger.error(ReindexAllHashes.class, e);
                }
                return Boolean.FALSE;
            } catch(LockTimeoutException e){
                if(logger.isErrorEnabled()){
                    logger.error(ReindexAllHashes.class, e);
                }
                return Boolean.FALSE;
            }
        }
        
    }
}
