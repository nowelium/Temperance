package temperance.ql;

import temperance.ql.node.ArgumentsNode;
import temperance.ql.node.FromNode;
import temperance.ql.node.FunctionNode;
import temperance.ql.node.KeyNode;
import temperance.ql.node.ParameterNode;
import temperance.ql.node.MengeNode;
import temperance.ql.node.Statement;

public interface Visitor<T, DATA> {
    
    public T visit(ArgumentsNode node, DATA data);
    
    public T visit(FromNode node, DATA data);
    
    public T visit(FunctionNode node, DATA data);
    
    public T visit(KeyNode node, DATA data);
    
    public T visit(ParameterNode node, DATA data);
    
    public T visit(MengeNode node, DATA data);
    
    public T visit(Statement node, DATA data);

}
