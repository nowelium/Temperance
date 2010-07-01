package temperance.function.impl;

import java.util.List;

import temperance.function.FunctionContext;
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
