package temperance.ql;

import java.util.List;

public interface InternalFunction {
    
    public List<String> in(String key, List<String> args);
    
    public List<String> not(String key, List<String> args);

}
