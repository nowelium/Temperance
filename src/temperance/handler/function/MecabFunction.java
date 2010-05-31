package temperance.handler.function;

import java.util.List;

import temperance.ft.Hashing;
import temperance.ft.Mecab;


public class MecabFunction extends AbstractTaggerFunction {
    
    public MecabFunction(FunctionContext context){
        super(context);
    }

    @Override
    protected Hashing createHashing(List<String> args) {
        return new Mecab(context.getHashFunction(), context.getTagger(), context.getNodeFilter());
    }
}
