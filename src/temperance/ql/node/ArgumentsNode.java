package temperance.ql.node;

import java.util.List;

import temperance.ql.Visitor;

public class ArgumentsNode implements Node {
    
    private final List<String> values;
    
    public ArgumentsNode(List<String> values){
        this.values = values;
    }
    
    public List<String> getValues(){
        return values;
    }
    
    public <T, DATA> T accept(Visitor<T, DATA> visitor, DATA data){
        return visitor.visit(this, data);
    }
    
    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder("ArgumentsStatement{");
        buf.append("values=").append(values);
        buf.append("}");
        return buf.toString();
    }
}
