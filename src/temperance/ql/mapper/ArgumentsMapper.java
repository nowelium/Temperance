package temperance.ql.mapper;

import java.util.List;

import org.codehaus.jparsec.functors.Map;

import temperance.ql.node.ArgumentsNode;

public class ArgumentsMapper implements Map<List<String>, ArgumentsNode> {
    public ArgumentsNode map(List<String> args){
        return new ArgumentsNode(args);
    }
}
