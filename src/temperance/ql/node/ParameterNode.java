package temperance.ql.node;

import temperance.ql.Visitor;

public class ParameterNode implements Node {

    private final ArgumentsNode args;
    
    public ParameterNode(ArgumentsNode args){
        this.args = args;
    }
    
    public ArgumentsNode getArgs(){
        return args;
    }
    
    public <T, DATA> T accept(Visitor<T, DATA> visitor, DATA data){
        return visitor.visit(this, data);
    }
    
    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder("FunctionParameter{");
        buf.append("args=").append(args);
        buf.append("}");
        return buf.toString();
    }
}
