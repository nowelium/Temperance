package temperance.ql;

import java.util.List;

import temperance.exception.ExecutionException;

public interface InternalFunction {
    
    public List<String> deleteIn(String key, List<String> args) throws ExecutionException;
    
    public List<String> deleteNot(String key, List<String> args) throws ExecutionException;
    
    public List<String> selectIn(String key, List<String> args) throws ExecutionException;
    
    public List<String> selectNot(String key, List<String> args) throws ExecutionException;

}
