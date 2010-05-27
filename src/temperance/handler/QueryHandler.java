package temperance.handler;

import java.util.List;

import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.MemcachedServerList;

import org.chasen.mecab.wrapper.Tagger;

import temperance.handler.function.DataFunction;
import temperance.handler.function.FunctionContext;
import temperance.handler.function.GeoPointFunction;
import temperance.handler.function.GramFunction;
import temperance.handler.function.MecabFunction;
import temperance.handler.function.ValueFunction;
import temperance.hash.Hash;
import temperance.hash.HashFunction;
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

public class QueryHandler implements QueryService.BlockingInterface {

    protected final Context context;
    
    protected final HashFunction hashFunction = Hash.MD5;
    
    protected final Tagger tagger = Tagger.create("-r /opt/local/etc/mecabrc");
    
    public QueryHandler(Context context){
        this.context = context;
    }
    
    protected MemcachedClient createMemcachedClient(){
        MemcachedClient client = new MemcachedClient();
        MemcachedServerList servers = client.getServerList();
        servers.parse(context.getMemcached());
        servers.push();
        return client;
    }
    
    public Response.Get get(RpcController controller, Request.Get request) throws ServiceException {
        String query = request.getQuery();
        QueryParser parser = new QueryParser();
        try {
            FunctionContext ctx = new FunctionContext();
            ctx.setClient(createMemcachedClient());
            ctx.setHashFunction(hashFunction);
            ctx.setTagger(tagger);
            FunctionFactoryFactory factory = new FunctionFactoryFactory(ctx);
            
            NodeVisitor visitor = new NodeVisitor();
            Statement stmt = parser.parse(query);
            List<String> results = stmt.accept(visitor, factory);
            
            return Response.Get.newBuilder().addAllValues(results).build();
        } catch(ParseException e){
            throw new ServiceException(e.getMessage());
        }
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
        
        private String key;
        
        private List<String> argsValue;
        
        private InternalFunction function;
        
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
            return node.getSet().each(new Switch(function, key, argsValue));
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
    }
    
    protected static class Switch implements SetFunction.Switch<List<String>> {
        
        private final InternalFunction function;
        
        private final String key;
        
        private final List<String> args;
        
        public Switch(InternalFunction function, String key, List<String> args){
            this.function = function;
            this.key = key;
            this.args = args;
        }
        public List<String> caseIn() {
            return function.in(key, args);
        }
        public List<String> caseNot() {
            return function.not(key, args);
        }
    }
}
