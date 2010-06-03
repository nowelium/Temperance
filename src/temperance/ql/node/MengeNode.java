package temperance.ql.node;

import temperance.ql.MengeType;
import temperance.ql.Visitor;

public class MengeNode implements Node {

    private final MengeType mengeType;
    
    public MengeNode(MengeType mengeType){
        this.mengeType = mengeType;
    }
    
    public MengeType getMengeType(){
        return mengeType;
    }
    
    public <T, DATA> T accept(Visitor<T, DATA> visitor, DATA data){
        return visitor.visit(this, data);
    }
    
    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder("MengeNode{");
        buf.append("mengeType=").append(mengeType);
        buf.append("}");
        return buf.toString();
    }
}
