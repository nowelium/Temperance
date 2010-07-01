package temperance.function.impl;

import java.util.List;

import temperance.function.FunctionContext;
import temperance.hashing.Hashing;

public class ConcreteHashingFunction extends AbstractTaggerFunction {

    protected final Hashing hashing;
    
    public ConcreteHashingFunction(FunctionContext context, Hashing hashing) {
        super(context);
        this.hashing = hashing;
    }

    @Override
    protected Hashing createHashing(List<String> args) {
        return hashing;
    }
}
