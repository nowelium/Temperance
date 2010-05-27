package temperance.ql.node;

import temperance.ql.Visitor;

public class FunctionNode implements Node {

    private final String functionName;
    
    private final ParameterNode parameter;
    
    public FunctionNode(String functionName, ParameterNode parameter){
        this.functionName = functionName;
        this.parameter = parameter;
    }
    
    public String getFunctionName(){
        return functionName;
    }
    
    public ParameterNode getParameter(){
        return parameter;
    }
    
    public <T, DATA> T accept(Visitor<T, DATA> visitor, DATA data){
        return visitor.visit(this, data);
    }
    
    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder("FunctionStatement{");
        buf.append("functionName=").append(functionName).append(",");
        buf.append("parameter=").append(parameter);
        buf.append("}");
        return buf.toString();
    }
}
