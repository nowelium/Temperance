package temperance.ql.mapper;

import java.util.List;

import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Tuple3;

import temperance.ql.SetFunction;
import temperance.ql.node.SetNode;

public class SetStatementMapper implements Map<Tuple3<List<Void>, SetFunction, List<Void>>, SetNode> {
    public SetNode map(Tuple3<List<Void>, SetFunction, List<Void>> tuple) {
        return new SetNode(tuple.b);
    }
}
