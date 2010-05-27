package temperance.handler;

import static org.codehaus.jparsec.Parsers.between;
import static org.codehaus.jparsec.Parsers.or;
import static org.codehaus.jparsec.Parsers.sequence;
import static org.codehaus.jparsec.Parsers.tuple;
import static org.codehaus.jparsec.Scanners.string;
import static org.codehaus.jparsec.Scanners.stringCaseInsensitive;

import java.util.List;

import org.codehaus.jparsec.OperatorTable;
import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Pair;
import org.codehaus.jparsec.functors.Tuple3;

public class QueryHandler {
    
    protected static final Map<String, Key> KeyParser = new Map<String, Key>() {
        public Key map(String args) {
            int sep = args.indexOf(':');
            return new Key(args.substring(0, sep), args.substring(sep + 1, args.length()));
        }
    };
    
    protected static final Map<Key, FromStatement> FromStatementParser = new Map<Key, FromStatement>() {
        public FromStatement map(Key key){
            return new FromStatement(key);
        }
    };
    
    protected static final Map<String, SetFunction> SetFunctionNameParser = new Map<String, SetFunction>(){
        public SetFunction map(String functionName) {
            return SetFunction.valueOf(functionName);
        }
    };
    
    protected static final Map<Tuple3<List<Void>, SetFunction, List<Void>>, SetStatement> SetStatementParser = new Map<Tuple3<List<Void>, SetFunction, List<Void>>, SetStatement> (){
        public SetStatement map(Tuple3<List<Void>, SetFunction, List<Void>> tuple) {
            return new SetStatement(tuple.b);
        }
    };
    
    protected static final Map<List<String>, ArgumentsStatement> ArgumentsParser = new Map<List<String>, ArgumentsStatement>(){
        public ArgumentsStatement map(List<String> args){
            return new ArgumentsStatement(args);
        }
    };
    
    protected static final Map<Tuple3<List<Void>, ArgumentsStatement, List<Void>>, ParameterStatement> FunctionParameterParser = new Map<Tuple3<List<Void>, ArgumentsStatement, List<Void>>, ParameterStatement> (){
        public ParameterStatement map(Tuple3<List<Void>, ArgumentsStatement, List<Void>> tuple) {
            return new ParameterStatement(tuple.b);
        }
    };
    
    protected static final Map<Pair<String, ParameterStatement>, FunctionStatement> FunctionParser = new Map<Pair<String, ParameterStatement>, FunctionStatement>(){
        public FunctionStatement map(Pair<String, ParameterStatement> pair) {
            return new FunctionStatement(pair.a, pair.b);
        }
    };
    
    protected static final Map<Tuple3<FromStatement, SetStatement, FunctionStatement>, Statement> StatementParser = new Map<Tuple3<FromStatement, SetStatement, FunctionStatement>, Statement>(){
        public Statement map(Tuple3<FromStatement, SetStatement, FunctionStatement> tuple) {
            return new Statement(tuple.a, tuple.b, tuple.c);
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
    // LETTER ::= <identifier>
    protected static final Parser<String> LETTER = Scanners.IDENTIFIER;
    // KEY ::= <LETTER>+ ":" <LETTER>
    protected static final Parser<Key> KEY = between(LETTER, string(":"), LETTER).source().map(KeyParser);
    // FROM ::= "from" <KEY>
    protected static final Parser<FromStatement> FROM = sequence(stringCaseInsensitive("FROM"), WHITESPACES, KEY).map(FromStatementParser);
    // SET ::= <FROM> (IN | NOT)
    protected static final Parser<SetStatement> SET = tuple(WHITESPACES, setFunctions(), WHITESPACES).map(SetStatementParser);
    // PARAMETER ::= <KEY> | <single_quote_str> | <LETTER> | <decimal>
    protected static final Parser<String> PARAMETER = KEY.source().or(Scanners.SINGLE_QUOTE_STRING).or(LETTER).or(Scanners.DECIMAL);
    // skip(PARAMETER_DELIMTER) ::= <WHITESPACE>? "," <WHITESPACE>
    protected static final Parser<List<Void>> PARAMETER_DELIMTER = sequence(WHITESPACES.optional(), string(","), WHITESPACES);
    // FUNCTION_ARGS ::= <PARAMETER> | <PARAMETER>, <PARAMETER>
    protected static final Parser<ArgumentsStatement> FUNCTION_ARGS = PARAMETER.sepBy(PARAMETER_DELIMTER).map(ArgumentsParser);
    // ARGS_OPEN ::= <WHITESPACE>? "(" <WHITESPACE>
    protected static final Parser<List<Void>> ARGS_OPEN = sequence(WHITESPACES.optional(), string("("), WHITESPACES);
    // ARGS_CLOE ::= <WHITESPACE>? ")" <WHITESPACE>
    protected static final Parser<List<Void>> ARGS_CLOSE = sequence(WHITESPACES.optional(), string(")"), WHITESPACES);
    // FUNCTION_PARAMETER ::= "(" <FUNCTION_ARGS> ")"
    protected static final Parser<ParameterStatement> FUNCTION_PARAMTER = tuple(ARGS_OPEN, FUNCTION_ARGS, ARGS_CLOSE).map(FunctionParameterParser);
    // FUNCTION ::= <FUNCTION_NAME> "(" <FUNCTION_PARAMTER> ")"
    protected static final Parser<FunctionStatement> FUNCTION = tuple(LETTER, FUNCTION_PARAMTER).map(FunctionParser);
    // STATEMENT ::= <FROM> <SET> <FUNCTION>
    protected static final Parser<Statement> STATEMENT = tuple(FROM, SET, FUNCTION).map(StatementParser);
    // PARSER ::= parser
    protected static final Parser<Statement> PARSER = parser(STATEMENT);
    
    protected static Parser<SetFunction> setFunctions(){
        return string(SetFunction.IN.name())
            .or(string(SetFunction.NOT.name()))
            .source().map(SetFunctionNameParser);
    }
    
    protected static Parser<Statement> parser(Parser<Statement> atom){
        Parser.Reference<Statement> ref = Parser.newReference();
        Parser<Statement> unit = ref.lazy().between(string("("), string(")")).or(atom);
        Parser<Statement> parser = new OperatorTable<Statement>().build(unit);
        ref.set(parser);
        return parser;
    }
    
    public static void main(String...args){
        System.out.println(KEY.parse("A:B"));
        System.out.println(FROM.parse("from A:B"));
        System.out.println(FUNCTION_ARGS.parse("aaa"));
        System.out.println(FUNCTION_ARGS.parse("123,456"));
        System.out.println(FUNCTION_ARGS.parse("A:B,C:D"));
        System.out.println(FUNCTION_ARGS.parse("A:B, C:D"));
        System.out.println(FUNCTION_ARGS.parse("A:B , C:D"));
        System.out.println(FUNCTION_PARAMTER.parse("(1,2)"));
        System.out.println(FUNCTION_PARAMTER.parse("(1, 2, 3)"));
        System.out.println(FUNCTION.parse("hoge(1, 2, 3)"));
        System.out.println(FUNCTION.parse("FOO (a, b, c)"));
        System.out.println(FUNCTION.parse("helloWorld (A:B, C:D, E:F)"));
        
        System.out.println(PARSER.parse("FROM A:B IN DATA(C:D)"));
        System.out.println(PARSER.parse("FROM A:B IN VALUES(1, 2, 3)"));
        System.out.println(PARSER.parse("FROM A:B IN GEOPOINT(1.123, 2.234)"));
        System.out.println(PARSER.parse("FROM A:B IN GEOPOINT(1.123, 2.234, 5)"));
        System.out.println(PARSER.parse("FROM A:B IN MECAB('hello world')"));
        System.out.println(PARSER.parse("FROM A:B IN BIGRAM('hello world')"));
        System.out.println(PARSER.parse("FROM A:B IN GRAM('hello world', 2)"));
    }
    
    protected static enum SetFunction {
        IN,
        NOT
    }
    
    protected static class Key {
        private final String namespace;
        private final String key;
        private Key(String namespace, String key){
            this.namespace = namespace;
            this.key = key;
        }
        @Override
        public String toString(){
            StringBuilder buf = new StringBuilder("Key{");
            buf.append("namespace=").append(namespace).append(",");
            buf.append("key=").append(key);
            buf.append("}");
            return buf.toString();
        }
    }
    
    protected static class FromStatement {
        private final Key key;
        private FromStatement(Key key){
            this.key = key;
        }
        @Override
        public String toString(){
            StringBuilder buf = new StringBuilder("FromStatement{");
            buf.append("key=").append(key);
            buf.append("}");
            return buf.toString();
        }
    }

    protected static class SetStatement {
        private final SetFunction set;
        private SetStatement(SetFunction set){
            this.set = set;
        }
        @Override
        public String toString(){
            StringBuilder buf = new StringBuilder("SetStatement{");
            buf.append("set=").append(set);
            buf.append("}");
            return buf.toString();
        }
    }
    
    protected static class ArgumentsStatement {
        private final List<String> values;
        private ArgumentsStatement(List<String> values){
            this.values = values;
        }
        @Override
        public String toString(){
            StringBuilder buf = new StringBuilder("ArgumentsStatement{");
            buf.append("values=").append(values);
            buf.append("}");
            return buf.toString();
        }
    }
    
    protected static class ParameterStatement {
        private final ArgumentsStatement args;
        private ParameterStatement(ArgumentsStatement args){
            this.args = args;
        }
        @Override
        public String toString(){
            StringBuilder buf = new StringBuilder("FunctionParameter{");
            buf.append("args=").append(args);
            buf.append("}");
            return buf.toString();
        }
    }
    
    protected static class FunctionStatement {
        private final String functionName;
        private final ParameterStatement parameter;
        private FunctionStatement(String functionName, ParameterStatement parameter){
            this.functionName = functionName;
            this.parameter = parameter;
        }
        @Override
        public String toString(){
            StringBuilder buf = new StringBuilder("FunctionStatement{");
            buf.append("functionName=").append(functionName).append(",");
            buf.append("parameter=").append(parameter);
            buf.append("}");
            return buf.toString();
        }
    }
    
    protected static class Statement {
        private final FromStatement from;
        private final SetStatement set;
        private final FunctionStatement function;
        private Statement(FromStatement from, SetStatement set, FunctionStatement function){
            this.from = from;
            this.set = set;
            this.function = function;
        }
        public String toString(){
            StringBuilder buf = new StringBuilder("Statement{");
            buf.append("from=").append(from).append(",");
            buf.append("set=").append(set).append(",");
            buf.append("function=").append(function);
            buf.append("}");
            return buf.toString();
        }
    }
    
}
