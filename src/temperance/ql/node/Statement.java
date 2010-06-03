package temperance.ql.node;

import temperance.ql.Visitor;

public class Statement implements Node {

    private final Boolean distinct;
    
    private final FromNode from;

    private final MengeNode menge;
    
    private final FunctionNode function;
    
    public Statement(Boolean distinct, FromNode from, MengeNode menge, FunctionNode function){
        this.distinct = distinct;
        this.from = from;
        this.menge = menge;
        this.function = function;
    }
    
    public boolean isDistinct(){
        return distinct.booleanValue();
    }
    
    public FromNode getFrom(){
        return from;
    }
    
    public MengeNode getMenge(){
        return menge;
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
        buf.append("menge=").append(menge).append(",");
        buf.append("function=").append(function);
        buf.append("}");
        return buf.toString();
    }
}
