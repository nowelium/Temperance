package temperance.ql;

import org.junit.Assert;
import org.junit.Test;

import temperance.ql.node.FromNode;
import temperance.ql.node.FunctionNode;
import temperance.ql.node.Statement;

public class QueryParserTest {
    
    @Test
    public void quote(){
        Assert.assertEquals(QueryParser.QUOTE_STRING.parse("\"HOGE\""), "HOGE");
        Assert.assertEquals(QueryParser.QUOTE_STRING.parse("'HOGE'"), "HOGE");
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
    }
    
    @Test
    public void function(){
        {
            FunctionNode function = QueryParser.FUNCTION.parse("DATA(B)");
            Assert.assertEquals(function.getFunctionName(), "DATA");
            Assert.assertEquals(function.getParameter().getArgs().getValues().get(0), "B");
        }
        {
            FunctionNode function = QueryParser.FUNCTION.parse("VALUES(1, 2, 3)");
            Assert.assertEquals(function.getFunctionName(), "VALUES");
            Assert.assertEquals(function.getParameter().getArgs().getValues().get(0), "1");
            Assert.assertEquals(function.getParameter().getArgs().getValues().get(1), "2");
            Assert.assertEquals(function.getParameter().getArgs().getValues().get(2), "3");
        }
        {
            FunctionNode function = QueryParser.FUNCTION.parse("GEOPOINT(1.123, 2.234, 12)");
            Assert.assertEquals(function.getFunctionName(), "GEOPOINT");
            Assert.assertEquals(function.getParameter().getArgs().getValues().get(0), "1.123");
            Assert.assertEquals(function.getParameter().getArgs().getValues().get(1), "2.234");
            Assert.assertEquals(function.getParameter().getArgs().getValues().get(2), "12");
        }
        {
            FunctionNode function = QueryParser.FUNCTION.parse("MECAB('hello world こんにちは')");
            Assert.assertEquals(function.getFunctionName(), "MECAB");
            Assert.assertEquals(function.getParameter().getArgs().getValues().get(0), "hello world こんにちは");
        }
        {
            FunctionNode function = QueryParser.FUNCTION.parse("GRAM('ほげ')");
            Assert.assertEquals(function.getFunctionName(), "GRAM");
            Assert.assertEquals(function.getParameter().getArgs().getValues().get(0), "ほげ");
        }
    }
    
    @Test
    public void query(){
        {
            Statement statement = QueryParser.PARSER.parse("FROM hoge IN GRAM('ほげ')");
            Assert.assertEquals(statement.getFrom().getKey().getKey(), "hoge");
            Assert.assertEquals(statement.getFunction().getFunctionName(), "GRAM");
        }
    }

}
