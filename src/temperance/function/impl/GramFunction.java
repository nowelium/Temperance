package temperance.function.impl;

import java.util.List;

import temperance.function.FunctionContext;
import temperance.hashing.GramHashing;
import temperance.hashing.Hashing;

public class GramFunction extends AbstractTaggerFunction {
    
    protected final int initialSeparator;
    
    public GramFunction(FunctionContext context){
        this(context, 2);
    }
    
    public GramFunction(FunctionContext context, int separatorValue){
        super(context);
        this.initialSeparator = separatorValue;
    }

    @Override
    protected Hashing createHashing(List<String> args) {
        final int split = getSplitValue(args);
        return new GramHashing(context.getHashFunction(), split);
    }

    private int getSplitValue(List<String> args){
        if(args.size() < 2){
            return initialSeparator;
        }
        
        String splitStr = args.get(1);
        try {
            return Integer.parseInt(splitStr);
        } catch(NumberFormatException e){
            return initialSeparator;
        }
    }

}
