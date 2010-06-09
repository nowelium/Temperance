package temperance.rpc.impl;

import java.util.Collections;
import java.util.List;

import org.chasen.mecab.wrapper.Tagger;

import temperance.exception.ExecutionException;
import temperance.exception.RpcException;
import temperance.ft.MecabNodeFilter;
import temperance.function.FunctionContext;
import temperance.function.FunctionFactory;
import temperance.function.InternalFunction;
import temperance.hash.HashFunction;
import temperance.memcached.ConnectionPool;
import temperance.ql.FunctionType;
import temperance.ql.MengeType;
import temperance.ql.QueryParser;
import temperance.ql.Visitor;
import temperance.ql.exception.ParseException;
import temperance.ql.node.ArgumentsNode;
import temperance.ql.node.FromNode;
import temperance.ql.node.FunctionNode;
import temperance.ql.node.KeyNode;
import temperance.ql.node.MengeNode;
import temperance.ql.node.ParameterNode;
import temperance.ql.node.Statement;
import temperance.rpc.Context;
import temperance.rpc.RpcQuery;
import temperance.util.Lists;

public class RpcQueryImpl implements RpcQuery {

    protected final QueryParser parser = new QueryParser();
    
    protected final Context context;
    
    protected final HashFunction hashFunction;
    
    protected final MecabNodeFilter nodeFilter;

    protected final Tagger tagger;
    
    protected final ConnectionPool pool;
    
    public RpcQueryImpl(Context context, ConnectionPool pool){
        this.context = context;
        this.hashFunction = context.getFullTextHashFunction();
        this.nodeFilter = context.getNodeFilter();
        this.tagger = Tagger.create("-r", context.getMecabrc());
        this.pool = pool;
    }
    
    public Response.Delete delete(Request.Delete request) throws RpcException {
        throw new RpcException("not yet implemented");
    }

    public Response.Select select(Request.Select request) throws RpcException {
        final String query = request.query;
        
        try {
            FunctionContext ctx = new FunctionContext();
            ctx.setPool(pool);
            ctx.setHashFunction(hashFunction);
            ctx.setTagger(tagger);
            ctx.setNodeFilter(nodeFilter);
            FunctionFactory factory = new FunctionFactory(ctx);
            
            NodeVisitor visitor = new NodeVisitor(Behavior.Select);
            Statement stmt = parser.parse(query);
            List<String> results = stmt.accept(visitor, factory);
            
            Response.Select response = Response.Select.newInstance();
            response.values = results;
            return response;
        } catch(ParseException e){
            throw new RpcException(e.getMessage());
        }
    }
    
    protected static enum Behavior {
        Select {
            public <RESULT, PARAMETER> RESULT each(Switch<RESULT, PARAMETER> sw, PARAMETER param){
                return sw.caseSelect(param);
            }
        },
        Delete {
            public <RESULT, PARAMETER> RESULT each(Switch<RESULT, PARAMETER> sw, PARAMETER param){
                return sw.caseDelete(param);
            }
        },
        ;
        
        public abstract <RESULT, PARAMETER> RESULT each(Switch<RESULT, PARAMETER> sw, PARAMETER param);
        
        public static interface Switch<RESULT, PARAMETER> {
            public RESULT caseSelect(PARAMETER parameter);
            public RESULT caseDelete(PARAMETER parameter);
        }
    }
    
    protected static class NodeVisitor implements Visitor<List<String>, FunctionFactory> {
        
        private final Behavior behavior;
        
        private String key;
        
        private List<String> argsValue;
        
        private InternalFunction functionFactory;
        
        private NodeVisitor(Behavior behavior){
            this.behavior = behavior;
        }
        
        public List<String> visit(ArgumentsNode node, FunctionFactory data) {
            this.argsValue = node.getValues();
            return null;
        }

        public List<String> visit(FunctionNode node, FunctionFactory data) {
            node.getParameter().accept(this, data);
            
            FunctionType queryFunc = node.getFunctionType();
            this.functionFactory = queryFunc.create(data);
            return null;
        }

        public List<String> visit(ParameterNode node, FunctionFactory data) {
            node.getArgs().accept(this, data);
            return null;
        }

        public List<String> visit(MengeNode node, FunctionFactory data) {
            InternalFunction.Command function = behavior.each(new BehaviorSwitch(), functionFactory);
            return node.getMengeType().each(new MengeSwitch(key, argsValue), function);
        }

        public List<String> visit(KeyNode node, FunctionFactory data) {
            this.key = node.getKey();
            return null;
        }
        
        public List<String> visit(FromNode node, FunctionFactory data) {
            node.getKey().accept(this, data);
            return null;
        }

        public List<String> visit(Statement node, FunctionFactory data) {
            node.getFrom().accept(this, data);
            node.getFunction().accept(this, data);
            List<String> result = node.getMenge().accept(this, data);
            if(node.isDistinct()){
                return Lists.unique(result);
            }
            return result;
        }
    }
    
    protected static class BehaviorSwitch implements Behavior.Switch<InternalFunction.Command, InternalFunction> {
        public InternalFunction.Command caseDelete(InternalFunction factory) {
            return factory.createDelete();
        }

        public InternalFunction.Command caseSelect(InternalFunction factory) {
            return factory.createSelect();
        }
    }
    
    protected static class MengeSwitch implements MengeType.Switch<List<String>, InternalFunction.Command> {
        protected final String key;
        protected final List<String> args;
        protected MengeSwitch(String key, List<String> args){
            this.key = key;
            this.args = args;
        }
        public List<String> caseAnd(InternalFunction.Command command) {
            try {
                return command.and(key, args);
            } catch(ExecutionException e){
                e.printStackTrace();
                return Collections.emptyList();
            }
        }

        public List<String> caseNot(InternalFunction.Command command) {
            try {
                return command.not(key, args);
            } catch(ExecutionException e){
                e.printStackTrace();
                return Collections.emptyList();
            }
        }

        public List<String> caseOr(InternalFunction.Command command) {
            try {
                return command.or(key, args);
            } catch(ExecutionException e){
                e.printStackTrace();
                return Collections.emptyList();
            }
        }
    }

}
