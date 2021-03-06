package temperance.storage;

import java.util.List;

import temperance.exception.LockTimeoutException;
import temperance.exception.MemcachedOperationException;
import temperance.hash.Hash;
import temperance.storage.TpList.TpListResult;

public interface TpFullText extends TpStorage {
    
    public long add(String key, Hash hash, String value, int expire) throws MemcachedOperationException, LockTimeoutException;
    
    public List<Long> addAll(String key, List<Hash> hashes, String value, int expire) throws MemcachedOperationException, LockTimeoutException;
    
    public List<Hash> getHashes(String key, long offset, long limit) throws MemcachedOperationException;
    
    public void getHashes(String key, long offset, long limit, StreamReader<Hash> reader) throws MemcachedOperationException;
    
    public List<Hash> getHashesByValue(String key, String value, long offset, long limit) throws MemcachedOperationException;
    
    public void getHashesByValue(String key, String value, long offset, long limit, StreamReader<Hash> reader) throws MemcachedOperationException;
    
    public List<TpListResult> getHashesByResult(String key, long offset, long limit) throws MemcachedOperationException;
    
    public void getHashesByResult(String key, long offset, long limit, StreamReader<TpListResult> reader) throws MemcachedOperationException;
    
    public List<String> getValues(String key, Hash hash, long offset, long limit) throws MemcachedOperationException;
    
    public void getValues(String key, Hash hash, long offset, long limit, StreamReader<String> reader) throws MemcachedOperationException;
    
    public List<TpListResult> getValuesByResult(String key, Hash hash, long offset, long limit) throws MemcachedOperationException;
    
    public void getValuesByResult(String key, Hash hash, long offset, long limit, StreamReader<TpListResult> reader) throws MemcachedOperationException;
    
    public long hashCount(String key) throws MemcachedOperationException;
    
    public long hashCountByValue(String key, String value) throws MemcachedOperationException;
    
    public long valueCount(String key, Hash hash) throws MemcachedOperationException;
    
    // TODO: all hash delte
    //public boolean delete(String key, int expire) throws MemcachedOperationException, LockTimeoutException;
    
    public boolean deleteByHash(String key, Hash hash, int expire) throws MemcachedOperationException, LockTimeoutException;
    
    public boolean deleteByValue(String key, String value, int expire) throws MemcachedOperationException, LockTimeoutException;
    
    public boolean deleteAtByHash(String key, Hash hash, long index, int expire) throws MemcachedOperationException, LockTimeoutException;
    
    // TODO: all hash reindex
    //public void reindex(String key) throws MemcachedOperationException, LockTimeoutException;
    
    public void reindexByHash(String key, Hash hash) throws MemcachedOperationException, LockTimeoutException;
    
}
