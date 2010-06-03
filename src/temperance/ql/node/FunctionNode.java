package temperance.ql.node;

import temperance.ql.FunctionType;
import temperance.ql.Visitor;

public class FunctionNode implements Node {

    private final FunctionType functionType;
    
    private final ParameterNode parameter;
    
    public FunctionNode(FunctionType functionType, ParameterNode parameter){
        this.functionType = functionType;
        this.parameter = parameter;
    }
    
    public FunctionType getFunctionType(){
        return functionType;
    }
    
    public ParameterNode getParameter(){
        return parameter;
    }
    
    public <T, DATA> T accept(Visitor<T, DATA> visitor, DATA data){
        return visitor.visit(this, data);
    }
    
    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder("FunctionNode{");
        buf.append("functionType=").append(functionType).append(",");
        buf.append("parameter=").append(parameter);
        buf.append("}");
        return buf.toString();
    }
}
