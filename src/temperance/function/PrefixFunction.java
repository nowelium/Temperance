package temperance.function;

import java.util.List;

import temperance.ft.Hashing;
import temperance.ft.PrefixHashing;

public class PrefixFunction extends AbstractTaggerFunction {

    public PrefixFunction(FunctionContext context) {
        super(context);
    }

    @Override
    protected Hashing createHashing(List<String> args) {
        return new PrefixHashing(context.getHashFunction());
    }

}
