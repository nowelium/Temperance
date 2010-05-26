package temperance.handler;

import static org.codehaus.jparsec.Parsers.or;
import static org.codehaus.jparsec.Parsers.pair;
import static org.codehaus.jparsec.Parsers.tuple;
import static org.codehaus.jparsec.Scanners.string;

import java.util.List;

import org.codehaus.jparsec.OperatorTable;
import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Pair;
import org.codehaus.jparsec.functors.Tuple3;


public class QueryHandler {
    
    protected static final Map<List<String>, Key> KeyParser = new Map<List<String>, Key>() {
        public Key map(List<String> args) {
            if(args.size() < 3){
                return new Key(args.get(0), args.get(1), null);
            }
            return new Key(args.get(0), args.get(1), args.get(2));
        }
    };
    
    protected static final Map<String, Functions> functionNameParser = new Map<String, Functions>(){
        public Functions map(String functionName) {
            return Functions.valueOf(functionName);
        }
    };
    
    protected static final Map<Pair<Functions, Key>, Function> FunctionParser = new Map<Pair<Functions, Key>, Function>() {
        public Function map(Pair<Functions, Key> pair) {
            return new Function(pair.a, pair.b);
        }
    };
    
    protected static final Map<Tuple3<Key, List<Void>, Function>, Statement> StatementParser = new Map<Tuple3<Key, List<Void>, Function>, Statement>() {
        public Statement map(Tuple3<Key, List<Void>, Function> tuple) {
            return new Statement(tuple.a, tuple.c);
        }
    };
    
    protected static final Terminals OPERATORS = Terminals.operators("(", ")");
    protected static final Parser<?> TOKENIZER = or(
        OPERATORS.tokenizer(),
        Terminals.StringLiteral.PARSER,
        Terminals.Identifier.TOKENIZER,
        Terminals.DecimalLiteral.TOKENIZER
    );
    
    protected static final Parser<List<Void>> WHITESPACES = Scanners.WHITESPACES.many();
    // KEYS ::= <LETTER>+ ":" <LETTER>
    protected static final Parser<Key> KEY = Scanners.IDENTIFIER.sepBy(string(":")).map(KeyParser);
    // FUNCTION_NAME ::= IN | NOT
    protected static final Parser<Functions> FUNCTION_NAME = functionNames();
    // FUNCTON_PARAMTER ::= "(" <KEY> ")"
    protected static final Parser<Key> FUNCTION_PARAMETER = KEY.between(string("("), string(")"));
    // FUNCTION ::= <FUNCTION_NAME> <FUNCTION_PARAMETER>
    protected static final Parser<Function> FUNCTION = pair(FUNCTION_NAME, FUNCTION_PARAMETER).map(FunctionParser);
    // STATEMENT ::= <KEY> <FUNCTION>
    protected static final Parser<Statement> STATEMENT = tuple(KEY, WHITESPACES, FUNCTION).map(StatementParser);
    // PARSER ::= parser
    protected static final Parser<Statement> PARSER = parser(STATEMENT);
    
    protected static Parser<Functions> functionNames(){
        return string(Functions.IN.name())
            .or(string(Functions.NOT.name()))
            .source().map(functionNameParser);
    }
    
    protected static Parser<Statement> parser(Parser<Statement> atom){
        Parser.Reference<Statement> ref = Parser.newReference();
        Parser<Statement> unit = ref.lazy().between(string("("), string(")")).or(atom);
        Parser<Statement> parser = new OperatorTable<Statement>().build(unit);
        ref.set(parser);
        return parser;
    }
    
    public static void main(String...args){
        Statement stmt = PARSER.parse("A:B IN(C:D)");
        System.out.println(stmt);
        
        /*
        STATEMENT stmt = PARSER.parse("hoge:aa IN(hoge:foo:bar)");
        System.out.println(stmt);
        */
    }
    
    protected static enum Functions {
        IN,
        NOT
    }
    
    protected static class Key {
        private final String namespace;
        private final String key;
        private final String id;
        private Key(String namespace, String key, String id){
            this.namespace = namespace;
            this.key = key;
            this.id = id;
        }
        @Override
        public String toString(){
            StringBuilder buf = new StringBuilder("KEY{");
            buf.append("namespace=").append(namespace).append(",");
            buf.append("key=").append(key).append(",");
            buf.append("id=").append(id);
            buf.append("}");
            return buf.toString();
        }
    }
    
    protected static class Function {
        private final Functions function;
        private final Key key;
        private Function(Functions function, Key key){
            this.function = function;
            this.key = key;
        }
        @Override
        public String toString(){
            StringBuilder buf = new StringBuilder("FUNCTION{");
            buf.append("function=").append(function).append(",");
            buf.append("key=").append(key);
            buf.append("}");
            return buf.toString();
        }
    }
    
    protected static class Statement {
        private final Key key;
        private final Function function;
        private Statement(Key key, Function function){
            this.key = key;
            this.function = function;
        }
        @Override
        public String toString(){
            StringBuilder buf = new StringBuilder("STATEMENT{");
            buf.append("key=").append(key).append(",");
            buf.append("function=").append(function);
            buf.append("}");
            return buf.toString();
        }
    }


}
