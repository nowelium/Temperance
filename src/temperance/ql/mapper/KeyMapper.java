package temperance.ql.mapper;

import org.codehaus.jparsec.functors.Map;

import temperance.ql.node.KeyNode;

public class KeyMapper implements Map<String, KeyNode> {
    public KeyNode map(String key) {
        return new KeyNode(key);
    }
}
