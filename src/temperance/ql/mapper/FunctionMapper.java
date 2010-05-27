package temperance.ql.mapper;

import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Pair;

import temperance.ql.node.FunctionNode;
import temperance.ql.node.ParameterNode;

public class FunctionMapper implements Map<Pair<String, ParameterNode>, FunctionNode> {
    public FunctionNode map(Pair<String, ParameterNode> pair) {
        return new FunctionNode(pair.a, pair.b);
    }
}
