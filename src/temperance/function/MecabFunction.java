package temperance.function;

import java.util.List;

import temperance.hashing.Hashing;
import temperance.hashing.MecabHashing;


public class MecabFunction extends AbstractTaggerFunction {
    
    public MecabFunction(FunctionContext context){
        super(context);
    }

    @Override
    protected Hashing createHashing(List<String> args) {
        return new MecabHashing(context.getHashFunction(), context.getTagger(), context.getNodeFilter());
    }
}
