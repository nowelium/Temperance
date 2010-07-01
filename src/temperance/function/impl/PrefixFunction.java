package temperance.function.impl;

import java.util.List;

import temperance.function.FunctionContext;
import temperance.hashing.Hashing;
import temperance.hashing.PrefixHashing;

public class PrefixFunction extends AbstractTaggerFunction {

    public PrefixFunction(FunctionContext context) {
        super(context);
    }

    @Override
    protected Hashing createHashing(List<String> args) {
        return new PrefixHashing(context.getHashFunction());
    }

}
