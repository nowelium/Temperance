package temperance.ql;

import java.util.Arrays;

import org.codehaus.jparsec.error.ParserException;
import org.junit.Assert;
import org.junit.Test;

import temperance.ql.node.FromNode;
import temperance.ql.node.FunctionNode;
import temperance.ql.node.KeyNode;
import temperance.ql.node.ParameterNode;
import temperance.ql.node.Statement;

public class QueryParserTest {
    
    @Test
    public void quote(){
        Assert.assertEquals(QueryParser.QUOTE_STRING.parse("\"HOGE\""), "HOGE");
        Assert.assertEquals(QueryParser.QUOTE_STRING.parse("'HOGE'"), "HOGE");
    }
    
    @Test
    public void key(){
        {
            KeyNode key = QueryParser.KEY.parse("A");
            Assert.assertEquals(key.getKey(), "A");
        }
        {
            KeyNode key = QueryParser.KEY.parse("'HELLO'");
            Assert.assertEquals(key.getKey(), "HELLO");
        }
        {
            KeyNode key = QueryParser.KEY.parse("HELLO:WORLD");
            Assert.assertEquals(key.getKey(), "HELLO:WORLD");
        }
        {
            KeyNode key = QueryParser.KEY.parse("'HELLO:WORLD'");
            Assert.assertEquals(key.getKey(), "HELLO:WORLD");
        }
        {
            KeyNode key = QueryParser.KEY.parse("HELLO@WORLD");
            Assert.assertEquals(key.getKey(), "HELLO@WORLD");
        }
        {
            try {
                KeyNode key = QueryParser.KEY.parse("HELLO[0]");
                Assert.assertEquals(key.getKey(), "HELLO[0]");
                Assert.fail();
            } catch(ParserException e){
            }
        }
    }
    
    @Test
    public void from(){
        {
            FromNode from = QueryParser.FROM.parse("FROM A");
            Assert.assertEquals(from.getKey().getKey(), "A");
        }
        {
            FromNode from = QueryParser.FROM.parse("FROM    B");
            Assert.assertEquals(from.getKey().getKey(), "B");
        }
        {
            FromNode from = QueryParser.FROM.parse("FROM 'Hoge:0'");
            Assert.assertEquals(from.getKey().getKey(), "Hoge:0");
        }
        {
            FromNode from = QueryParser.FROM.parse("FROM Hoge:Foo");
            Assert.assertEquals(from.getKey().getKey(), "Hoge:Foo");
        }
        {
            FromNode from = QueryParser.FROM.parse("FROM Hoge@0");
            Assert.assertEquals(from.getKey().getKey(), "Hoge@0");
        }
    }
    
    @Test
    public void parameter(){
        {
            ParameterNode parameter = QueryParser.FUNCTION_PARAMTER.parse("(A, B)");
            Assert.assertEquals(parameter.getArgs().getValues(), Arrays.asList("A", "B"));
        }
        {
            ParameterNode parameter = QueryParser.FUNCTION_PARAMTER.parse("(1, 2)");
            Assert.assertEquals(parameter.getArgs().getValues(), Arrays.asList("1", "2"));
        }
        {
            ParameterNode parameter = QueryParser.FUNCTION_PARAMTER.parse("(hoge)");
            Assert.assertEquals(parameter.getArgs().getValues(), Arrays.asList("hoge"));
        }
        {
            ParameterNode parameter = QueryParser.FUNCTION_PARAMTER.parse("('foo')");
            Assert.assertEquals(parameter.getArgs().getValues(), Arrays.asList("foo"));
        }
        {
            ParameterNode parameter = QueryParser.FUNCTION_PARAMTER.parse("('bar', 'baz')");
            Assert.assertEquals(parameter.getArgs().getValues(), Arrays.asList("bar", "baz"));
        }
        {
            ParameterNode parameter = QueryParser.FUNCTION_PARAMTER.parse("(A:0,A:1)");
            Assert.assertEquals(parameter.getArgs().getValues(), Arrays.asList("A:0", "A:1"));
        }
        {
            ParameterNode parameter = QueryParser.FUNCTION_PARAMTER.parse("(A:0,   A:1)");
            Assert.assertEquals(parameter.getArgs().getValues(), Arrays.asList("A:0", "A:1"));
        }
    }
    
    @Test
    public void function(){
        {
            FunctionNode function = QueryParser.FUNCTION.parse("DATA(B)");
            Assert.assertEquals(function.getFunctionType(), FunctionType.DATA);
            Assert.assertEquals(function.getParameter().getArgs().getValues().get(0), "B");
        }
        {
            FunctionNode function = QueryParser.FUNCTION.parse("VALUE(1, 2, 3)");
            Assert.assertEquals(function.getFunctionType(), FunctionType.VALUE);
            Assert.assertEquals(function.getParameter().getArgs().getValues().get(0), "1");
            Assert.assertEquals(function.getParameter().getArgs().getValues().get(1), "2");
            Assert.assertEquals(function.getParameter().getArgs().getValues().get(2), "3");
        }
        {
            FunctionNode function = QueryParser.FUNCTION.parse("GEOPOINT(1.123, 2.234, 12)");
            Assert.assertEquals(function.getFunctionType(), FunctionType.GEOPOINT);
            Assert.assertEquals(function.getParameter().getArgs().getValues().get(0), "1.123");
            Assert.assertEquals(function.getParameter().getArgs().getValues().get(1), "2.234");
            Assert.assertEquals(function.getParameter().getArgs().getValues().get(2), "12");
        }
        {
            FunctionNode function = QueryParser.FUNCTION.parse("MECAB('hello world こんにちは')");
            Assert.assertEquals(function.getFunctionType(), FunctionType.MECAB);
            Assert.assertEquals(function.getParameter().getArgs().getValues().get(0), "hello world こんにちは");
        }
        {
            FunctionNode function = QueryParser.FUNCTION.parse("GRAM('ほげ')");
            Assert.assertEquals(function.getFunctionType(), FunctionType.GRAM);
            Assert.assertEquals(function.getParameter().getArgs().getValues().get(0), "ほげ");
        }
    }
    
    @Test
    public void query(){
        {
            Statement statement = QueryParser.PARSER.parse("FROM hoge IN GRAM('ほげ')");
            Assert.assertEquals(statement.getFrom().getKey().getKey(), "hoge");
            Assert.assertEquals(statement.getMenge().getMengeType(), MengeType.IN);
            Assert.assertEquals(statement.getFunction().getFunctionType(), FunctionType.GRAM);
            Assert.assertEquals(statement.getFunction().getParameter().getArgs().getValues().get(0), "ほげ");
        }
        {
            Statement statement = QueryParser.PARSER.parse("FROM hoge IN VALUE(1, 2)");
            Assert.assertEquals(statement.getFrom().getKey().getKey(), "hoge");
            Assert.assertEquals(statement.getMenge().getMengeType(), MengeType.IN);
            Assert.assertEquals(statement.getFunction().getFunctionType(), FunctionType.VALUE);
            Assert.assertEquals(statement.getFunction().getParameter().getArgs().getValues().get(0), "1");
            Assert.assertEquals(statement.getFunction().getParameter().getArgs().getValues().get(1), "2");
        }
        {
            Statement statement = QueryParser.PARSER.parse("FROM hoge NOT VALUE(1, 2)");
            Assert.assertEquals(statement.getFrom().getKey().getKey(), "hoge");
            Assert.assertEquals(statement.getMenge().getMengeType(), MengeType.NOT);
            Assert.assertEquals(statement.getFunction().getFunctionType(), FunctionType.VALUE);
            Assert.assertEquals(statement.getFunction().getParameter().getArgs().getValues().get(0), "1");
            Assert.assertEquals(statement.getFunction().getParameter().getArgs().getValues().get(1), "2");
        }
        {
            Statement statement = QueryParser.PARSER.parse("FROM hoge@a NOT VALUE(1, 2)");
            Assert.assertEquals(statement.getFrom().getKey().getKey(), "hoge@a");
            Assert.assertEquals(statement.getMenge().getMengeType(), MengeType.NOT);
            Assert.assertEquals(statement.getFunction().getFunctionType(), FunctionType.VALUE);
            Assert.assertEquals(statement.getFunction().getParameter().getArgs().getValues().get(0), "1");
            Assert.assertEquals(statement.getFunction().getParameter().getArgs().getValues().get(1), "2");
        }
    }
    
    @Test
    public void query_distinct(){
        {
            Statement statement = QueryParser.PARSER.parse("DISTINCT FROM hoge IN VALUE(1)");
            Assert.assertEquals(statement.getFrom().getKey().getKey(), "hoge");
            Assert.assertEquals(statement.isDistinct(), true);
            Assert.assertEquals(statement.getMenge().getMengeType(), MengeType.IN);
            Assert.assertEquals(statement.getFunction().getFunctionType(), FunctionType.VALUE);
        }
        {
            Statement statement = QueryParser.PARSER.parse("FROM hoge IN VALUE(1)");
            Assert.assertEquals(statement.getFrom().getKey().getKey(), "hoge");
            Assert.assertEquals(statement.isDistinct(), false);
            Assert.assertEquals(statement.getMenge().getMengeType(), MengeType.IN);
            Assert.assertEquals(statement.getFunction().getFunctionType(), FunctionType.VALUE);
        }
    }
    
    @Test
    public void parser(){
    }

}
