package temperance.ql.node;

import temperance.ql.SetFunction;
import temperance.ql.Visitor;

public class SetNode implements Node {

    private final SetFunction set;
    
    public SetNode(SetFunction set){
        this.set = set;
    }
    
    public SetFunction getSet(){
        return set;
    }
    
    public <T, DATA> T accept(Visitor<T, DATA> visitor, DATA data){
        return visitor.visit(this, data);
    }
    
    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder("SetStatement{");
        buf.append("set=").append(set);
        buf.append("}");
        return buf.toString();
    }
}
