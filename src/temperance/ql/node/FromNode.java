package temperance.ql.node;

import temperance.ql.Visitor;

public class FromNode implements Node {

    private final KeyNode key;
    
    public FromNode(KeyNode key){
        this.key = key;
    }
    
    public KeyNode getKey(){
        return key;
    }
    
    public <T, DATA> T accept(Visitor<T, DATA> visitor, DATA data){
        return visitor.visit(this, data);
    }
    
    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder("FromNode{");
        buf.append("key=").append(key);
        buf.append("}");
        return buf.toString();
    }
}
