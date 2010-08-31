package temperance.core.command;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import temperance.core.Command;
import temperance.core.CommandPool;
import temperance.core.Pooling;
import temperance.exception.LockTimeoutException;
import temperance.exception.MemcachedOperationException;
import temperance.hash.Hash;
import temperance.storage.TpFullText;
import temperance.storage.TpList.TpListResult;
import temperance.storage.impl.MemcachedFullText;

public class FullTextCommand {
    
    protected final BlockingQueue<KeyValueExpire> hashesByValueQueue = new LinkedBlockingQueue<KeyValueExpire>();
    
    protected final BlockingQueue<KeyHashes> valuesByResultQueue = new LinkedBlockingQueue<KeyHashes>();
    
    protected final BlockingQueue<KeyHashResults> deleteAtByHashQueue = new LinkedBlockingQueue<KeyHashResults>();
    
    protected final CommandPool pool;
    
    private static final Log logger = LogFactory.getLog(FullTextCommand.class);
    
    private final Pooling pooling;
    
    public FullTextCommand(CommandPool pool, Pooling pooling){
        this.pool = pool;
        this.pooling = pooling;
    }
    
    public void init() {
        pool.run(new getHashesByValue());
        pool.run(new getValuesByResult());
        pool.run(new deleteAtByHash());
    }
    
    public void deleteAllValues(String key, int expire, String value){
        hashesByValueQueue.offer(new KeyValueExpire(key, expire, value));
    }
    
    private static class KeyValueExpire {
        private final String key;
        private final int expire;
        private final String value;
        private KeyValueExpire(String key, int expire, String value){
            this.key = key;
            this.value = value;
            this.expire = expire;
        }
    }
    
    private static class KeyHashes {
        private final KeyValueExpire kve;
        private final List<Hash> hashes;
        private KeyHashes(KeyValueExpire kve, List<Hash> hashes){
            this.kve = kve;
            this.hashes = hashes;
        }
    }
    
    private static class KeyHashResults {
        private final KeyValueExpire kve;
        private final Hash hash;
        private final List<TpListResult> results;
        private KeyHashResults(KeyValueExpire kve, Hash hash, List<TpListResult> results){
            this.kve = kve;
            this.hash = hash;
            this.results = results;
        }
    }
    
    private class getHashesByValue implements Runnable {
        public void run(){
            try {
                while(true){
                    try {
                        KeyValueExpire kve = hashesByValueQueue.take();
                        
                        final TpFullText ft = new MemcachedFullText(pooling.getConnectionPool());
                        final long hashCount = ft.hashCount(kve.key);
                        for(long i = 0; i < hashCount; i += Command.SPLIT){
                            List<Hash> hashes = ft.getHashes(kve.key, i, Command.SPLIT);
                            valuesByResultQueue.offer(new KeyHashes(kve, hashes));
                        }
                    } catch(MemcachedOperationException e){
                        logger.error(e.getMessage(), e);
                    }
                }
            } catch(InterruptedException e){
                throw new RuntimeException(e);
            }
        }
    }
    
    private class getValuesByResult implements Runnable {
        public void run(){
            try {
                while(true){
                    try {
                        final KeyHashes keyHashes = valuesByResultQueue.take();
                        
                        final TpFullText ft = new MemcachedFullText(pooling.getConnectionPool());
                        final KeyValueExpire kve = keyHashes.kve;
                        for(Hash hash: keyHashes.hashes){
                            final long valueCount = ft.valueCount(kve.key, hash);
                            for(long i = 0; i < valueCount; i += Command.SPLIT){
                                List<TpListResult> results = ft.getValuesByResult(kve.key, hash, i, Command.SPLIT);
                                deleteAtByHashQueue.offer(new KeyHashResults(kve, hash, results));
                            }
                        }
                    } catch(MemcachedOperationException e){
                        logger.error(e.getMessage(), e);
                    }
                }
            } catch(InterruptedException e){
                throw new RuntimeException(e);
            }
        }
    }
    
    private class deleteAtByHash implements Runnable {
        public void run(){
            try {
                while(true){
                    try {
                        KeyHashResults keyHashresults = deleteAtByHashQueue.take();
                        
                        final TpFullText ft = new MemcachedFullText(pooling.getConnectionPool());
                        final KeyValueExpire kve = keyHashresults.kve;
                        for(TpListResult result: keyHashresults.results){
                            if(kve.value.equals(result.getValue())){
                                ft.deleteAtByHash(kve.key, keyHashresults.hash, result.getIndex(), kve.expire);
                            }
                        }
                    } catch(LockTimeoutException e){
                        logger.error(e.getMessage(), e);
                    } catch(MemcachedOperationException e){
                        logger.error(e.getMessage(), e);
                    }
                }
            } catch(InterruptedException e){
                throw new RuntimeException(e);
            }
        }
    }

}
