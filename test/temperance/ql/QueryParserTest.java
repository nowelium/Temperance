package temperance.ql;

import org.junit.Assert;
import org.junit.Test;

import temperance.ql.node.FromNode;

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

}
