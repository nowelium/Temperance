package temperance.ql;

import static org.codehaus.jparsec.Parsers.or;
import static org.codehaus.jparsec.Parsers.sequence;
import static org.codehaus.jparsec.Parsers.tuple;
import static org.codehaus.jparsec.Scanners.string;
import static org.codehaus.jparsec.Scanners.stringCaseInsensitive;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import temperance.ql.mapper.FunctionTypeMapper;
import temperance.ql.mapper.KeyMapper;
import temperance.ql.mapper.MengeStatementMapper;
import temperance.ql.mapper.MengeTypeMapper;
import temperance.ql.mapper.QuoteStringMapper;
import temperance.ql.mapper.StatementMapper;
import temperance.ql.node.ArgumentsNode;
import temperance.ql.node.FromNode;
import temperance.ql.node.FunctionNode;
import temperance.ql.node.KeyNode;
import temperance.ql.node.MengeNode;
import temperance.ql.node.ParameterNode;
import temperance.ql.node.Statement;
import temperance.util.Lists;
import temperance.util.SoftReferenceMap;

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
    protected static final Parser<String> LETTER = Scanners.IDENTIFIER.or(Scanners.DECIMAL);
    // SPECIAL_CHARS ::= ":" | "_" | "@" | "{" | "}" | "[" | "]"
    protected static final Parser<String> SPECIAL_CHARS = specialChars(":", "@", "_").source();
    // QUOTE_STRING ::= single_quote_string | double_quote_string
    protected static final Parser<String> QUOTE_STRING = quoteString();
    // SPECIAL_LETTER ::= <LETTER> <SPECIAL_CHARS> <LETTER>
    protected static final Parser<String> SPECIAL_LETTER = SPECIAL_CHARS.between(LETTER, LETTER).source();
    // KEY_STRING ::= <QUOTE_STRING> | <LETTER>
    protected static final Parser<String> KEY_STRING = QUOTE_STRING.or(SPECIAL_LETTER).or(LETTER);
    // KEY ::= keyString()
    protected static final Parser<KeyNode> KEY = SPECIAL_LETTER.or(KEY_STRING).map(new KeyMapper());
    // FROM ::= "from" <KEY>
    protected static final Parser<FromNode> FROM = sequence(stringCaseInsensitive("FROM"), WHITESPACES, KEY).map(new FromStatementMapper());
    // SET ::= <FROM> (IN | NOT)
    protected static final Parser<MengeNode> MENGE = tuple(WHITESPACES, mengeType(), WHITESPACES).map(new MengeStatementMapper());
    // PARAMETER ::= <KEY_STRING> | <decimal>
    protected static final Parser<String> PARAMETER = KEY_STRING.or(Scanners.DECIMAL);
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
    // FUNCTON_NAME ::= functionType()
    protected static final Parser<FunctionType> FUNCTION_NAME = functionType();
    // FUNCTION ::= <FUNCTION_NAME> "(" <FUNCTION_PARAMTER> ")"
    protected static final Parser<FunctionNode> FUNCTION = tuple(FUNCTION_NAME, FUNCTION_PARAMTER).map(new FunctionMapper());
    // DISTINCT ::= "DISTINCT"
    protected static final Parser<Boolean> DISTINCT = sequence(stringCaseInsensitive("DISTINCT"), WHITESPACES).source().retn(Boolean.TRUE);
    // STATEMENT ::= <FROM> <SET> <FUNCTION> <DISTINCT>?
    protected static final Parser<Statement> STATEMENT = tuple(DISTINCT.optional(), FROM, MENGE, FUNCTION).map(new StatementMapper());
    // PARSER ::= parser
    protected static final Parser<Statement> PARSER = parser(STATEMENT);
    
    // parser cache
    protected final SoftReferenceMap<String, Statement> parserCache = new SoftReferenceMap<String, Statement>();
    
    // log
    protected final Log logger = LogFactory.getLog(QueryParser.class);
    
    protected static Parser<String> quoteString(){
        return Scanners.SINGLE_QUOTE_STRING.map(new QuoteStringMapper('\''))
            .or(Scanners.DOUBLE_QUOTE_STRING.map(new QuoteStringMapper('"')));
    }
    
    protected static Parser<Void> specialChars(String...chars){
        List<Parser<Void>> parsers = Lists.newArrayList();
        for(String ch: chars){
            parsers.add(string(ch));
        }
        return or(parsers);
    }
    
    protected static Parser<MengeType> mengeType(){
        List<Parser<Void>> parsers = Lists.newArrayList();
        for(MengeType type: MengeType.values()){
            parsers.add(stringCaseInsensitive(type.name()));
        }
        return or(parsers).source().map(new MengeTypeMapper());
    }
    
    protected static Parser<FunctionType> functionType(){
        List<Parser<Void>> parsers = Lists.newArrayList();
        for(FunctionType type: FunctionType.values()){
            parsers.add(stringCaseInsensitive(type.name()));
        }
        return or(parsers).source().map(new FunctionTypeMapper());

    }
    
    protected static Parser<Statement> parser(Parser<Statement> atom){
        Parser.Reference<Statement> ref = Parser.newReference();
        Parser<Statement> unit = ref.lazy().between(string("("), string(")")).or(atom);
        Parser<Statement> parser = new OperatorTable<Statement>().build(unit);
        ref.set(parser);
        return parser;
    }
    
    public Statement parse(String source) throws ParseException {
        if(logger.isDebugEnabled()){
            logger.debug("input query: '" + source + "'");
        }
        
        if("".equals(source)){
            throw new ParseException("empty query");
        }
        
        try {
            if(parserCache.containsKey(source)){
                return parserCache.get(source);
            }
            
            if(logger.isDebugEnabled()){
                logger.debug("parsing query");
            }
            
            Statement stmt = PARSER.parse(source);
            parserCache.put(source, stmt);
            
            if(logger.isDebugEnabled()){
                logger.debug("statement: " + stmt);
            }
            return stmt;
        } catch(ParserException e) {
            throw new ParseException(e.getMessage() + " in query: " + source);
        }
    }

}
