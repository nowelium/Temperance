package temperance.ql.node;

import temperance.ql.Visitor;

public interface Node {
    public <T, DATA> T accept(Visitor<T, DATA> visitor, DATA data);
}
