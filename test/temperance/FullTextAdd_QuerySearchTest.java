package temperance;

import java.util.HashMap;

import libmemcached.wrapper.type.BehaviorType;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import temperance.core.Configure;
import temperance.core.Pooling;
import temperance.exception.RpcException;
import temperance.hash.Digest;
import temperance.hashing.MecabHashing;
import temperance.rpc.RpcFullText;
import temperance.rpc.RpcQuery;
import temperance.rpc.impl.RpcFullTextImpl;
import temperance.rpc.impl.RpcQueryImpl;

public class FullTextAdd_QuerySearchTest {
    
    private Configure configure;
    
    private Pooling pooling;
    
    @Before
    public void before(){
        configure = new Configure();
        configure.setFullTextHashFunction(Digest.MD5);
        configure.setMemcached("localhost:11211");
        configure.setNodeFilter(MecabHashing.Filter.Nouns);
        configure.setPoolBehaviors(new HashMap<BehaviorType, Boolean>(){
            private static final long serialVersionUID = 1L;
            {
                put(BehaviorType.BUFFER_REQUESTS, Boolean.FALSE);
            }
        });
        
        pooling = new Pooling(configure);
        pooling.init();
    }
    
    @After
    public void after(){
        pooling.getConnectionPool().get().getStorage().flush(0);
        pooling.destroy();
    }
    
    @Test
    public void logic_ft_query() throws RpcException {
        RpcFullText ft = new RpcFullTextImpl(configure, pooling);
        {
            RpcFullText.Request.Add add = RpcFullText.Request.Add.newInstance();
            add.key = "test";
            add.str = "hoge foo bar";
            add.parser = RpcFullText.Request.Parser.HASH_SSV;
            add.value = "12345";
            add.expire = 0;
            //
            add.asyncRequest = false;
            ft.add(add);
        }
        {
            RpcFullText.Request.Add add = RpcFullText.Request.Add.newInstance();
            add.key = "test";
            add.str = "baz foo quux";
            add.parser = RpcFullText.Request.Parser.HASH_SSV;
            add.value = "67890";
            add.expire = 0;
            //
            add.asyncRequest = false;
            ft.add(add);
        }

        RpcQuery query = new RpcQueryImpl(configure, pooling);
        {
            RpcQuery.Request.Select select = RpcQuery.Request.Select.newInstance();
            select.query = "FROM test IN SSV('foo')";
            RpcQuery.Response.Select response = query.select(select);
            System.out.println(response.values);
            Assert.assertTrue(response.values.contains("12345"));
            Assert.assertTrue(response.values.contains("67890"));
        }
        
        RpcFullText.Request.DeleteByValue deleteByValue = RpcFullText.Request.DeleteByValue.newInstance();
        deleteByValue.key = "test";
        deleteByValue.value = "67890";
        deleteByValue.expire = 0;
        // 
        deleteByValue.asyncRequest = false;
        ft.deleteByValue(deleteByValue);
        
        {
            RpcQuery.Request.Select select = RpcQuery.Request.Select.newInstance();
            select.query = "FROM test IN SSV('foo')";
            RpcQuery.Response.Select response = query.select(select);
            System.out.println(response.values);
            Assert.assertTrue(response.values.contains("12345"));
            Assert.assertFalse(response.values.contains("67890"));
        }
    }

}
