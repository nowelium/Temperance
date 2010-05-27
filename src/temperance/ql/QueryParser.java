package temperance.ql;

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
import org.codehaus.jparsec.error.ParserException;

import temperance.ql.exception.ParseException;
import temperance.ql.mapper.ArgumentsMapper;
import temperance.ql.mapper.FromStatementMapper;
import temperance.ql.mapper.FunctionMapper;
import temperance.ql.mapper.FunctionParameterMapper;
import temperance.ql.mapper.KeyMapper;
import temperance.ql.mapper.SetFunctionNameMapper;
import temperance.ql.mapper.SetStatementMapper;
import temperance.ql.mapper.SingleQuoteStringMapper;
import temperance.ql.mapper.StatementMapper;
import temperance.ql.node.ArgumentsNode;
import temperance.ql.node.FromNode;
import temperance.ql.node.FunctionNode;
import temperance.ql.node.KeyNode;
import temperance.ql.node.ParameterNode;
import temperance.ql.node.SetNode;
import temperance.ql.node.Statement;

public class QueryParser {
    
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
    // QUOTE_STRING ::= single_quote_string
    protected static final Parser<String> QUOTE_STRING = Scanners.SINGLE_QUOTE_STRING.map(new SingleQuoteStringMapper());
    // KEY ::= <LETTER>+ ":" <LETTER>
    protected static final Parser<KeyNode> KEY = LETTER.map(new KeyMapper());
    // FROM ::= "from" <KEY>
    protected static final Parser<FromNode> FROM = sequence(stringCaseInsensitive("FROM"), WHITESPACES, KEY).map(new FromStatementMapper());
    // SET ::= <FROM> (IN | NOT)
    protected static final Parser<SetNode> SET = tuple(WHITESPACES, setFunctions(), WHITESPACES).map(new SetStatementMapper());
    // PARAMETER ::= <KEY> | <single_quote_str> | <LETTER> | <decimal>
    protected static final Parser<String> PARAMETER = KEY.source().or(QUOTE_STRING).or(LETTER).or(Scanners.DECIMAL);
    // skip(PARAMETER_DELIMTER) ::= <WHITESPACE>? "," <WHITESPACE>
    protected static final Parser<List<Void>> PARAMETER_DELIMTER = sequence(WHITESPACES.optional(), string(","), WHITESPACES);
    // FUNCTION_ARGS ::= <PARAMETER> | <PARAMETER>, <PARAMETER>
    protected static final Parser<ArgumentsNode> FUNCTION_ARGS = PARAMETER.sepBy(PARAMETER_DELIMTER).map(new ArgumentsMapper());
    // ARGS_OPEN ::= <WHITESPACE>? "(" <WHITESPACE>
    protected static final Parser<List<Void>> ARGS_OPEN = sequence(WHITESPACES.optional(), string("("), WHITESPACES);
    // ARGS_CLOE ::= <WHITESPACE>? ")" <WHITESPACE>
    protected static final Parser<List<Void>> ARGS_CLOSE = sequence(WHITESPACES.optional(), string(")"), WHITESPACES);
    // FUNCTION_PARAMETER ::= "(" <FUNCTION_ARGS> ")"
    protected static final Parser<ParameterNode> FUNCTION_PARAMTER = tuple(ARGS_OPEN, FUNCTION_ARGS, ARGS_CLOSE).map(new FunctionParameterMapper());
    // FUNCTION ::= <FUNCTION_NAME> "(" <FUNCTION_PARAMTER> ")"
    protected static final Parser<FunctionNode> FUNCTION = tuple(LETTER, FUNCTION_PARAMTER).map(new FunctionMapper());
    // STATEMENT ::= <FROM> <SET> <FUNCTION>
    protected static final Parser<Statement> STATEMENT = tuple(FROM, SET, FUNCTION).map(new StatementMapper());
    // PARSER ::= parser
    protected static final Parser<Statement> PARSER = parser(STATEMENT);
    
    protected static Parser<SetFunction> setFunctions(){
        return string(SetFunction.IN.name())
            .or(string(SetFunction.NOT.name()))
            .source().map(new SetFunctionNameMapper());
    }
    
    protected static Parser<Statement> parser(Parser<Statement> atom){
        Parser.Reference<Statement> ref = Parser.newReference();
        Parser<Statement> unit = ref.lazy().between(string("("), string(")")).or(atom);
        Parser<Statement> parser = new OperatorTable<Statement>().build(unit);
        ref.set(parser);
        return parser;
    }
    
    public Statement parse(String source) throws ParseException {
        if("".equals(source)){
            throw new ParseException("empty query");
        }
        try {
            return PARSER.parse(source);
        } catch(ParserException e) {
            throw new ParseException(e.getMessage());
        }
    }

}
