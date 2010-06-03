package temperance.ql.node;

import temperance.ql.MengeType;
import temperance.ql.Visitor;

public class MengeNode implements Node {

    private final MengeType menge;
    
    public MengeNode(MengeType menge){
        this.menge = menge;
    }
    
    public MengeType getMenge(){
        return menge;
    }
    
    public <T, DATA> T accept(Visitor<T, DATA> visitor, DATA data){
        return visitor.visit(this, data);
    }
    
    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder("SetStatement{");
        buf.append("menge=").append(menge);
        buf.append("}");
        return buf.toString();
    }
}
