package temperance.ql.node;

import temperance.ql.Visitor;

public class Statement implements Node {

    private final FromNode from;

    private final SetNode set;
    
    private final FunctionNode function;
    
    public Statement(FromNode from, SetNode set, FunctionNode function){
        this.from = from;
        this.set = set;
        this.function = function;
    }
    
    public FromNode getFrom(){
        return from;
    }
    
    public SetNode getSet(){
        return set;
    }
    
    public FunctionNode getFunction(){
        return function;
    }
    
    public <T, DATA> T accept(Visitor<T, DATA> visitor, DATA data){
        return visitor.visit(this, data);
    }
    
    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder("Statement{");
        buf.append("from=").append(from).append(",");
        buf.append("set=").append(set).append(",");
        buf.append("function=").append(function);
        buf.append("}");
        return buf.toString();
    }
}
