package temperance.function;

import temperance.ql.FunctionType;

public class FunctionFactory implements FunctionType.Factory {
    
    protected final FunctionContext context;
    
    public FunctionFactory(FunctionContext context){
        this.context = context;
    }
    
    public InternalFunction createBigram() {
        return new GramFunction(context, 2);
    }
    
    public InternalFunction createGram() {
        return new GramFunction(context);
    }

    public InternalFunction createData() {
        return new DataFunction(context);
    }

    public InternalFunction createGeoPoint() {
        return new GeoPointFunction(context);
    }

    public InternalFunction createMecab() {
        return new MecabFunction(context);
    }

    public InternalFunction createValue() {
        return new ValueFunction(context);
    }
    
    public InternalFunction createPrefix() {
        return new PrefixFunction(context);
    }
    
    public InternalFunction createLevenshteinDistance(){
        throw new RuntimeException("not yet implemented");
    }
}
