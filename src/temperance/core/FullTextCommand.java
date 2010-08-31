package temperance.core;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import temperance.exception.LockTimeoutException;
import temperance.exception.MemcachedOperationException;
import temperance.hash.Hash;
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
        return submit(new DeleteAllValues(thread, connection, key, expire, value));
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
                for(long i = 0; i < hashCount; i += SPLIT){
                    List<Hash> hashes = ft.getHashes(key, i, SPLIT);
                    perform(ft, hashes);
                }
                return Boolean.TRUE;
            } catch(MemcachedOperationException e){
                if(logger.isErrorEnabled()){
                    logger.error(DeleteAll.class, e);
                }
                return Boolean.FALSE;
            } catch(LockTimeoutException e){
                if(logger.isErrorEnabled()){
                    logger.error(DeleteAll.class, e);
                }
                return Boolean.FALSE;
            }
        }
        protected void perform(TpFullText ft, List<Hash> hashes) throws MemcachedOperationException, LockTimeoutException {
            for(Hash hash: hashes){
                ft.deleteByHash(key, hash, expire);
            }
        }
    }
    
    protected static class DeleteAllValues extends SubCommand<Boolean> {
        protected final ThreadPool thpool;
        protected final ConnectionPool pool;
        protected final String key;
        protected final int expire;
        protected final String value;
        protected DeleteAllValues(ThreadPool thpool, ConnectionPool pool, String key, int expire, String value) {
            this.thpool = thpool;
            this.pool = pool;
            this.key = key;
            this.expire = expire;
            this.value = value;
        }
        public Boolean apply() throws LockTimeoutException, MemcachedOperationException {
            final TpFullText ft = new MemcachedFullText(pool);
            // FIXME: delete all performance improve
            try {
                // first. delete hash by value
                final long hashCount = ft.hashCountByValue(key, value);
                
                logger.info("perform value -> getHashesByValue");
                final LinkedList<Future<List<Hash>>> hashes = Lists.newLinkedList();
                for(long i = 0; i < hashCount; i += SPLIT){
                    hashes.add(thpool.submit(new getHashesByValue(i, SPLIT)));
                }
                logger.info("perform value: done");
                
                logger.info("perform hash -> deleteByValue");
                final LinkedList<Future<List<Future<Result>>>> results = Lists.newLinkedList();
                for(Future<List<Hash>> hash: hashes){
                    results.add(thpool.submit(new perform(hash)));
                }
                logger.info("perform hash: done");
                
                //
                // wait all futures
                //
                try {
                    // TODO: await all...
                    logger.info("await all: delete");
                    for(Future<List<Future<Result>>> future: results){
                        logger.debug("await: delete");
                        List<Future<Result>> deletes = future.get();
                        for(Future<Result> deleted: deletes){
                            Result result = deleted.get();
                            if(logger.isDebugEnabled()){
                                logger.debug(new StringBuilder()
                                    .append("FullText deleteAllValues ")
                                    .append("{")
                                    .append("key: ").append(key).append(",")
                                    .append("value: ").append(value).append(",")
                                    .append("expire: ").append(expire)
                                    .append("}")
                                    .append(" result => {")
                                    .append("hash => ").append(result.hash.hashValue()).append(",")
                                    .append("proceed => ").append(result.proceed)
                                    .append("}")
                                );
                            }
                        }
                        logger.debug("await: delete done");
                    }
                    logger.info("await all: delete done");
                } finally {
                    // second. delete value
                    logger.info("delete value");
                    ft.deleteByValue(key, value, expire);
                }
                
                // TODO: third. delete hash!! 
                //
                //
                
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
            } catch(ExecutionException e){
                if(logger.isErrorEnabled()){
                    logger.error(DeleteAllValues.class, e);
                }
                return Boolean.FALSE;
            } catch(InterruptedException e){
                if(logger.isErrorEnabled()){
                    logger.error(DeleteAllValues.class, e);
                }
                return Boolean.FALSE;
            }
        }
        private class getHashesByValue implements Callable<List<Hash>> {
            private final long index;
            private final long limit;
            private getHashesByValue(long index, long limit){
                this.index = index;
                this.limit = limit;
            }
            public List<Hash> call() throws Exception {
                final TpFullText _ft = new MemcachedFullText(pool);
                return _ft.getHashesByValue(key, value, index, limit);
            }
        }
        private class perform implements Callable<List<Future<Result>>> {
            private final Future<List<Hash>> future;
            private perform(Future<List<Hash>> future){
                this.future = future;
            }
            public List<Future<Result>> call() throws Exception {
                final List<Future<Result>> deleted = Lists.newArrayList();
                final List<Hash> hashes = future.get();
                final TpFullText _ft = new MemcachedFullText(pool);
                for(Hash hash: hashes){
                    final LinkedList<Future<List<TpListResult>>> queue = Lists.newLinkedList();
                    final long valueCount = _ft.valueCount(key, hash);
                    for(long i = 0; i < valueCount; i += SPLIT){
                        queue.add(thpool.submit(new getValuesByResult(hash, i)));
                    }
                    
                    for(Future<List<TpListResult>> future: queue){
                        deleted.add(thpool.submit(new deleteAtByHash(hash, future)));
                    }
                }
                return deleted;
            }
        }
        private class getValuesByResult implements Callable<List<TpListResult>> {
            private final Hash hash;
            private final long index;
            private getValuesByResult(Hash hash, long index){
                this.hash = hash;
                this.index = index;
            }
            public List<TpListResult> call() throws Exception {
                final TpFullText _ft = new MemcachedFullText(pool);
                return _ft.getValuesByResult(key, hash, index, SPLIT);
            }
        }
        private static class Result {
            protected final Hash hash;
            protected final Boolean proceed;
            private Result(Hash hash, Boolean proceed){
                this.hash = hash;
                this.proceed = proceed;
            }
        }
        private class deleteAtByHash extends SubCommand<Result> {
            private final Hash hash;
            private final Future<List<TpListResult>> future;
            private deleteAtByHash(final Hash hash, Future<List<TpListResult>> future){
                this.hash = hash;
                this.future = future;
            }
            @Override
            public Result apply() throws LockTimeoutException, MemcachedOperationException {
                try {
                    final List<TpListResult> results = future.get();
                    final TpFullText _ft = new MemcachedFullText(pool);
                    for(TpListResult result: results){
                        if(value.equals(result.getValue())){
                            _ft.deleteAtByHash(key, hash, result.getIndex(), expire);
                        }
                    }
                    return new Result(hash, Boolean.TRUE);
                } catch(InterruptedException e){
                    if(logger.isErrorEnabled()){
                        logger.error(deleteAtByHash.class, e);
                    }
                    return new Result(hash, Boolean.FALSE);
                } catch(ExecutionException e){
                    if(logger.isErrorEnabled()){
                        logger.error(deleteAtByHash.class, e);
                    }
                    return new Result(hash, Boolean.FALSE);
                } catch(LockTimeoutException e){
                    if(logger.isErrorEnabled()){
                        logger.error(deleteAtByHash.class, e);
                    }
                    return new Result(hash, Boolean.FALSE);
                } catch(MemcachedOperationException e){
                    if(logger.isErrorEnabled()){
                        logger.error(deleteAtByHash.class, e);
                    }
                    return new Result(hash, Boolean.FALSE);
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
                List<Hash> results = ft.getHashes(key, i, SPLIT);
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
                    List<Hash> hashes = ft.getHashes(key, i, SPLIT);
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
