package temperance.ql.mapper;

import org.codehaus.jparsec.functors.Map;

import temperance.ql.node.FromNode;
import temperance.ql.node.KeyNode;

public class FromStatementMapper implements Map<KeyNode, FromNode> {
    public FromNode map(KeyNode key){
        return new FromNode(key);
    }
}
