package temperance.handler;

import java.util.Collections;
import java.util.List;

import org.chasen.mecab.wrapper.Tagger;

import temperance.ft.MecabNodeFilter;
import temperance.handler.function.Behavior;
import temperance.handler.function.DataFunction;
import temperance.handler.function.FunctionContext;
import temperance.handler.function.GeoPointFunction;
import temperance.handler.function.GramFunction;
import temperance.handler.function.MecabFunction;
import temperance.handler.function.PrefixFunction;
import temperance.handler.function.ValueFunction;
import temperance.handler.function.exception.ExecutionException;
import temperance.hash.HashFunction;
import temperance.memcached.Pool;
import temperance.protobuf.Query.QueryService;
import temperance.protobuf.Query.Request;
import temperance.protobuf.Query.Response;
import temperance.ql.InternalFunction;
import temperance.ql.QueryFunction;
import temperance.ql.QueryParser;
import temperance.ql.SetFunction;
import temperance.ql.Visitor;
import temperance.ql.exception.ParseException;
import temperance.ql.node.ArgumentsNode;
import temperance.ql.node.FromNode;
import temperance.ql.node.FunctionNode;
import temperance.ql.node.KeyNode;
import temperance.ql.node.ParameterNode;
import temperance.ql.node.SetNode;
import temperance.ql.node.Statement;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

public class QueryServiceHandler implements QueryService.BlockingInterface {

protected final Context context;
    
    protected final HashFunction hashFunction;
    
    protected final MecabNodeFilter nodeFilter;

    protected final Tagger tagger;
    
    protected final Pool pool;
    
    public QueryServiceHandler(Context context, Pool pool){
        this.context = context;
        this.hashFunction = context.getFullTextHashFunction();
        this.nodeFilter = context.getNodeFilter();
        this.tagger = Tagger.create("-r", context.getMecabrc());
        this.pool = pool;
    }
    
    public Response.Get get(RpcController controller, Request.Get request) throws ServiceException {
        final String query = request.getQuery();
        final QueryParser parser = new QueryParser();
        try {
            FunctionContext ctx = new FunctionContext();
            ctx.setPool(pool);
            ctx.setHashFunction(hashFunction);
            ctx.setTagger(tagger);
            ctx.setNodeFilter(nodeFilter);
            FunctionFactoryFactory factory = new FunctionFactoryFactory(ctx);
            
            NodeVisitor visitor = new NodeVisitor(Behavior.Select);
            Statement stmt = parser.parse(query);
            List<String> results = stmt.accept(visitor, factory);
            
            return Response.Get.newBuilder().addAllValues(results).build();
        } catch(ParseException e){
            throw new ServiceException(e.getMessage());
        }
    }
    
    public Response.Delete delete(RpcController controller, Request.Delete request) throws ServiceException {
        throw new ServiceException("not yet implemented");
    }
    
    protected static class FunctionFactoryFactory {
        
        private final FunctionContext context;
        
        private FunctionFactoryFactory(FunctionContext context){
            this.context = context;
        }
        
        public FunctionFactory create(){
            return new FunctionFactory(context);
        }
    }
    
    protected static class NodeVisitor implements Visitor<List<String>, FunctionFactoryFactory> {
        
        private final Behavior behavior;
        
        private String key;
        
        private List<String> argsValue;
        
        private InternalFunction function;
        
        private NodeVisitor(Behavior behavior){
            this.behavior = behavior;
        }
        
        public List<String> visit(ArgumentsNode node, FunctionFactoryFactory data) {
            this.argsValue = node.getValues();
            return null;
        }

        public List<String> visit(FunctionNode node, FunctionFactoryFactory data) {
            QueryFunction queryFunc = QueryFunction.valueOf(node.getFunctionName());
            this.function = queryFunc.create(data.create());
            return node.getParameter().accept(this, data);
        }

        public List<String> visit(ParameterNode node, FunctionFactoryFactory data) {
            return node.getArgs().accept(this, data);
        }

        public List<String> visit(SetNode node, FunctionFactoryFactory data) {
            return node.getSet().each(new Switch(behavior, function, key, argsValue));
        }

        public List<String> visit(KeyNode node, FunctionFactoryFactory data) {
            this.key = node.getKey();
            return null;
        }
        
        public List<String> visit(FromNode node, FunctionFactoryFactory data) {
            return node.getKey().accept(this, data);
        }

        public List<String> visit(Statement node, FunctionFactoryFactory data) {
            node.getFrom().accept(this, data);
            node.getFunction().accept(this, data);
            return node.getSet().accept(this, data);
        }
    }
    
    protected static class FunctionFactory implements QueryFunction.Factory {

        private final FunctionContext context;
        
        private FunctionFactory(FunctionContext context){
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
            return null;
        }
    }
    
    protected static class Switch implements SetFunction.Switch<List<String>> {
        
        private final Behavior behavior;
        
        private final InternalFunction function;
        
        private final String key;
        
        private final List<String> args;
        
        public Switch(Behavior behavior, InternalFunction function, String key, List<String> args){
            this.behavior = behavior;
            this.function = function;
            this.key = key;
            this.args = args;
        }
        public List<String> caseIn() {
            return behavior.each(new Behavior.Switch<List<String>>(){
                public List<String> caseDelete() {
                    try {
                        return function.deleteIn(key, args);
                    } catch(ExecutionException e){
                        e.printStackTrace();
                        return Collections.emptyList();
                    }
                }
                public List<String> caseSelect() {
                    try {
                        return function.selectIn(key, args);
                    } catch(ExecutionException e){
                        e.printStackTrace();
                        return Collections.emptyList();
                    }
                }
            });
        }
        public List<String> caseNot() {
            try {
                if(Behavior.Delete.equals(behavior)){
                    return function.deleteNot(key, args);
                }
                return function.selectNot(key, args);
            } catch(ExecutionException e){
                e.printStackTrace();
                return Collections.emptyList();
            }
        }
    }
}
