package temperance.ql.mapper;

import java.util.List;

import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Tuple3;

import temperance.ql.node.ArgumentsNode;
import temperance.ql.node.ParameterNode;

public class FunctionParameterMapper implements Map<Tuple3<List<Void>, ArgumentsNode, List<Void>>, ParameterNode> {
    public ParameterNode map(Tuple3<List<Void>, ArgumentsNode, List<Void>> tuple) {
        return new ParameterNode(tuple.b);
    }
}
