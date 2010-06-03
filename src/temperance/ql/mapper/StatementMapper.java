package temperance.ql.mapper;

import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Tuple4;

import temperance.ql.node.FromNode;
import temperance.ql.node.FunctionNode;
import temperance.ql.node.MengeNode;
import temperance.ql.node.Statement;

public class StatementMapper implements Map<Tuple4<Boolean, FromNode, MengeNode, FunctionNode>, Statement> {
    public Statement map(Tuple4<Boolean, FromNode, MengeNode, FunctionNode> tuple) {
        Boolean distinct = Boolean.FALSE;
        if(tuple.a != null){
            distinct = tuple.a;
        }
        return new Statement(distinct, tuple.b, tuple.c, tuple.d);
    }
}
