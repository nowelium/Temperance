package temperance.ql.mapper;

import java.util.List;

import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Tuple3;

import temperance.ql.MengeType;
import temperance.ql.node.MengeNode;

public class MengeStatementMapper implements Map<Tuple3<List<Void>, MengeType, List<Void>>, MengeNode> {
    public MengeNode map(Tuple3<List<Void>, MengeType, List<Void>> tuple) {
        return new MengeNode(tuple.b);
    }
}
