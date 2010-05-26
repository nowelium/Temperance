package temperance.handler;

import static org.codehaus.jparsec.Parsers.or;
import static org.codehaus.jparsec.Parsers.pair;
import static org.codehaus.jparsec.Scanners.pattern;
import static org.codehaus.jparsec.Scanners.string;

import java.util.List;

import org.codehaus.jparsec.OperatorTable;
import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Pair;
import org.codehaus.jparsec.pattern.CharPredicates;
import org.codehaus.jparsec.pattern.Patterns;


public class QueryHandler {
    
    private static class KEY {
        private final String namespace;
        private final String key;
        private final String id;
        private KEY(String namespace, String key, String id){
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
    
    private static class FUNCTION {
        private final String functionName;
        private final KEY key;
        private FUNCTION(String functionName, KEY key){
            this.functionName = functionName;
            this.key = key;
        }
        @Override
        public String toString(){
            StringBuilder buf = new StringBuilder("FUNCTION{");
            buf.append("functionName=").append(functionName).append(",");
            buf.append("key=").append(key);
            buf.append("}");
            return buf.toString();
        }
    }
    
    private static class STATEMENT {
        private final KEY key;
        private final FUNCTION function;
        private STATEMENT(KEY key, FUNCTION function){
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
    
    private static final Map<List<String>, KEY> KeyParser = new Map<List<String>, KEY>() {
        public KEY map(List<String> args) {
            if(args.size() < 3){
                return new KEY(args.get(0), args.get(1), null);
            }
            return new KEY(args.get(0), args.get(1), args.get(2));
        }
    };
    
    private static final Map<Pair<String, KEY>, FUNCTION> FunctionParser = new Map<Pair<String, KEY>, FUNCTION>() {
        public FUNCTION map(Pair<String, KEY> pair) {
            return new FUNCTION(pair.a, pair.b);
        }
    };
    
    private static final Map<Pair<KEY, FUNCTION>, STATEMENT> StatementParser = new Map<Pair<KEY, FUNCTION>, STATEMENT>() {
        public STATEMENT map(Pair<KEY, FUNCTION> pair) {
            return new STATEMENT(pair.a, pair.b);
        }
    };
    
    private static final Terminals OPERATORS = Terminals.operators("(", ")");
    private static final Parser<?> TOKENIZER = or(
        OPERATORS.tokenizer(),
        Terminals.StringLiteral.PARSER,
        Terminals.Identifier.TOKENIZER,
        Terminals.DecimalLiteral.TOKENIZER
    );
    private static final Parser<Void> IGNORED = Scanners.WHITESPACES.skipMany();
    // LETTER ::= alpha*
    private static final Parser<String> LETTER = pattern(Patterns.isChar(CharPredicates.IS_ALPHA_), "letter").source();
    // KEYS ::= <LETTER>+ ":" <LETTER>
    private static final Parser<KEY> KEY = LETTER.many().source().sepBy(string(":")).map(KeyParser);
    // FUNCTION_NAME ::= IN | NOT
    private static final Parser<String> FUNCTION_NAME = string("IN").or(string("NOT")).source();
    // FUNCTON_PARAMTER ::= "(" <KEY> ")"
    private static final Parser<KEY> FUNCTION_PARAMETER = KEY.between(string("("), string(")"));
    // FUNCTION ::= <FUNCTION_NAME> <FUNCTION_PARAMETER>
    private static final Parser<FUNCTION> FUNCTION = pair(FUNCTION_NAME, FUNCTION_PARAMETER).map(FunctionParser);
    // STATEMENT ::= <KEY> <FUNCTION>
    private static final Parser<STATEMENT> STATEMENT = pair(KEY, FUNCTION).map(StatementParser);
    // PARSER ::= parser
    private static final Parser<STATEMENT> PARSER = parser(STATEMENT).from(TOKENIZER, IGNORED);
    
    private static Parser<STATEMENT> parser(Parser<STATEMENT> atom){
        Parser.Reference<STATEMENT> ref = Parser.newReference();
        Parser<STATEMENT> unit = ref.lazy().between(string("("), string(")")).or(atom);
        Parser<STATEMENT> parser = new OperatorTable<STATEMENT>().build(unit);
        ref.set(parser);
        return parser;
    }
    
    public static void main(String...args){
        FUNCTION func = FUNCTION.parse("IN(hoge:foo:bar)");
        System.out.println(func);
        //STATEMENT stmt = STATEMENT.parse("hoge:aa IN(hoge:foo:bar)");
        //System.out.println(stmt);
    }

}
