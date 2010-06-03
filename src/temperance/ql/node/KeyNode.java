package temperance.ql.node;

import temperance.ql.Visitor;

public class KeyNode implements Node {

    private final String key;
    
    public KeyNode(String key){
        this.key = key;
    }
    
    public String getKey(){
        return key;
    }
    
    public <T, DATA> T accept(Visitor<T, DATA> visitor, DATA data){
        return visitor.visit(this, data);
    }
    
    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder("KeyNode{");
        buf.append("key=").append(key);
        buf.append("}");
        return buf.toString();
    }
}
