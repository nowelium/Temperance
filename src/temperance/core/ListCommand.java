package temperance.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import temperance.exception.LockTimeoutException;
import temperance.exception.MemcachedOperationException;
import temperance.storage.TpList;
import temperance.storage.TpList.TpListResult;
import temperance.storage.TpStorage.StreamReader;
import temperance.storage.impl.MemcachedList;
import temperance.util.Lists;

public class ListCommand extends Command {
    
    protected static final Log logger = LogFactory.getLog(ListCommand.class);
    
    protected final ThreadPool thread;

    protected final ConnectionPool connection;
    
    public ListCommand(Pooling pooling){
        this.thread = pooling.getThreadPool();
        this.connection = pooling.getConnectionPool();
    }
    
    public Future<Long> add(final String key, final String value, final int expire){
        return submit(new AddValue(connection, key, value, expire));
    }
    
    public Future<Boolean> delete(final String key, final int expire){
        return submit(new Delete(connection, key, expire));
    }
    
    public Future<Boolean> deleteAllValues(final String key, final int expire, final String value){
        return submit(new DeleteAllValues(connection, key, expire, value));
    }
    
    public Future<Boolean> reindex(final String key) {
        return submit(new Reindex(connection, key));
    }
    
    public Future<List<String>> get(final String key, final long offset, final long limit){
        return submit(new LimitGetValues(connection, key, offset, limit));
    }
    
    public Future<List<String>> getAll(final String key){
        return submit(new GetAllValues(connection, key));
    }
    
    public List<Future<List<String>>> getAll(final List<String> keys) {
        final List<Future<List<String>>> futures = Lists.newArrayList();
        for(String key: keys){
            futures.add(submit(new GetAllValues(connection, key)));
        }
        return futures;
    }
    
    public void filterAll(final String key, final Filter filter) throws InterruptedException {
        filterAll(Arrays.asList(key), filter);
    }
    
    public void filterAll(final List<String> keys, final Filter filter) throws InterruptedException {
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
    
    protected static class AddValue extends SubCommand<Long> {
        private final ConnectionPool pool;
        private final String key;
        private final String value;
        private final int expire;
        protected AddValue(ConnectionPool pool, String key, String value, int expire){
            this.pool = pool;
            this.key = key;
            this.value = value;
            this.expire = expire;
        }
        public Long apply() throws LockTimeoutException, MemcachedOperationException {
            final TpList list = new MemcachedList(pool);
            final long id = list.add(key, value, expire);
            return Long.valueOf(id);
        }
    }
    
    protected static class Delete extends SubCommand<Boolean> {
        private final ConnectionPool pool;
        private final String key;
        private final int expire;
        protected Delete(ConnectionPool pool, String key, int expire){
            this.pool = pool;
            this.key = key;
            this.expire = expire;
        }
        public Boolean apply() throws LockTimeoutException, MemcachedOperationException {
            final TpList list = new MemcachedList(pool);
            final boolean deleted = list.delete(key, expire);
            return Boolean.valueOf(deleted);
        }
    }
    
    protected static class DeleteAllValues extends SubCommand<Boolean> {
        private final ConnectionPool pool;
        private final String key;
        private final int expire;
        private final String value;
        protected DeleteAllValues(ConnectionPool pool, String key, int expire, String value){
            this.pool = pool;
            this.key = key;
            this.expire = expire;
            this.value = value;
        }
        public Boolean apply() throws LockTimeoutException, MemcachedOperationException {
            // TODO: logic
            final TpList list = new MemcachedList(pool);
            final long count = list.count(key);
            try {
                final performValues performValues = new performValues();
                for(long i = 0; i < count; i += SPLIT){
                    long limit = SPLIT;
                    // get(offset, limit) -> valueOf(SPLIT) not exceed valueOf(limit): thx messy
                    if(count <= (i + SPLIT)){
                        limit = count - i;
                    }
                    
                    list.getByResult(key, i, limit, performValues);
                }
                
                return Boolean.TRUE;
            } catch(MemcachedOperationException e){
                if(logger.isErrorEnabled()){
                    logger.error(DeleteAllValues.class, e);
                }
                
                return Boolean.FALSE;
            }
        }
        private class performValues implements StreamReader<TpListResult> {
            private final Log logger = LogFactory.getLog(performValues.class);
            private final TpList list = new MemcachedList(pool);
            
            @Override
            public void read(TpListResult result) {
                if(!value.equals(result.getValue())){
                    return;
                }
                
                try {
                    list.deleteAt(key, result.getIndex(), expire);
                } catch (MemcachedOperationException e) {
                    if(logger.isErrorEnabled()){
                        logger.error(performValues.class, e);
                    }
                } catch (LockTimeoutException e) {
                    if(logger.isErrorEnabled()){
                        logger.error(performValues.class, e);
                    }
                }
            }
        }
    }
    
    protected static class Reindex extends SubCommand<Boolean> {
        private final ConnectionPool pool;
        private final String key;
        protected Reindex(ConnectionPool pool, String key){
            this.pool = pool;
            this.key = key;
        }
        public Boolean apply() throws LockTimeoutException, MemcachedOperationException {
            // TODO: handle Exception
            
            final TpList list = new MemcachedList(pool);
            try {
                list.reindex(key);
                
                return Boolean.TRUE;
            } catch(MemcachedOperationException e){
                if(logger.isErrorEnabled()){
                    logger.error(Reindex.class, e);
                }
                
                return Boolean.FALSE;
            }
        }
    }
    
    protected static class GetAllValues extends SubCommand<List<String>> {
        private final ConnectionPool pool;
        private final String key;
        protected GetAllValues(ConnectionPool pool, String key){
            this.pool = pool;
            this.key = key;
        }
        public List<String> apply() throws LockTimeoutException, MemcachedOperationException {
            final TpList list = new MemcachedList(pool);
            final List<String> returnValue = Lists.newArrayList();
            final long count = list.count(key);
            if(count < 1){
                return Collections.emptyList();
            }
            for(long i = 0; i < count; i += SPLIT){
                long limit = SPLIT;
                // get(offset, limit) -> valueOf(SPLIT) not exceed valueOf(limit): thx messy
                if(count <= (i + SPLIT)){
                    limit = count - i;
                }
                
                List<String> results = list.get(key, i, limit);
                returnValue.addAll(results);
            }
            return returnValue;
        }
    }
    
    protected static class AllFilterValues extends SubCommand<Void> {
        private final ConnectionPool pool;
        private final String key;
        private final Filter filter;
        protected AllFilterValues(ConnectionPool pool, String key, Filter filter){
            this.pool = pool;
            this.key = key;
            this.filter = filter;
        }
        public Void apply() throws LockTimeoutException, MemcachedOperationException {
            final TpList list = new MemcachedList(pool);
            final long count = list.count(key);
            for(long i = 0; i < count; i += SPLIT){
                long limit = SPLIT;
                // get(offset, limit) -> valueOf(SPLIT) not exceed valueOf(limit): thx messy
                if(count <= (i + SPLIT)){
                    limit = count - i;
                }
                
                List<String> results = list.get(key, i, limit);
                filter.execute(results);
            }
            return null;
        }
    }

    protected static class LimitGetValues extends SubCommand<List<String>> {
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
        public List<String> apply() throws LockTimeoutException, MemcachedOperationException {
            final TpList list = new MemcachedList(pool);
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
                // get(offset, limit) -> valueOf(SPLIT) not exceed valueOf(limit): thx messy
                if(limit < SPLIT){
                    splitLimit = limit;
                }
                
                results.addAll(list.get(key, offset + i, splitLimit));
            }
            return results;
        }
    }

}
