package temperance.ql.mapper;

import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Tuple3;

import temperance.ql.node.FromNode;
import temperance.ql.node.FunctionNode;
import temperance.ql.node.SetNode;
import temperance.ql.node.Statement;

public class StatementMapper implements Map<Tuple3<FromNode, SetNode, FunctionNode>, Statement> {
    public Statement map(Tuple3<FromNode, SetNode, FunctionNode> tuple) {
        return new Statement(tuple.a, tuple.b, tuple.c);
    }
}