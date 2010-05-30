package temperance.ql;

import java.util.List;

public interface InternalFunction {
    
    public List<String> deleteIn(String key, List<String> args);
    
    public List<String> deleteNot(String key, List<String> args);
    
    public List<String> selectIn(String key, List<String> args);
    
    public List<String> selectNot(String key, List<String> args);

}
