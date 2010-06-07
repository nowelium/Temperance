package temperance.rpc;

import java.util.List;

import temperance.exception.RpcException;
import temperance.util.Lists;

public interface RpcFullText {
    
    public Response.Set set(Request.Set request) throws RpcException;
    
    public Response.Search search(Request.Search request) throws RpcException;
    
    public static abstract class Request {
        public static enum Parser {
            MECAB,
            BIGRAM,
            PREFIX
        }
        public static class Set {
            public String key;
            public String str;
            public String value;
            public int expire = 86400;
            public Parser parser = Parser.MECAB;
            
            private Set(){
                // nop
            }
            public static Set newInstance(){
                return new Set();
            }
        }
        public static class Search {
            public String key;
            public String str;
            public Parser parser = Parser.MECAB;
            
            private Search(){
                // nop
            }
            public static Search newInstance(){
                return new Search();
            }
        }
    }
    
    public static abstract class Response {
        public static class Set {
            public boolean succeed;
            
            private Set(){
                // nop
            }
            public static Set newInstance(){
                return new Set();
            }
        }
        public static class Search {
            public List<String> values = Lists.newArrayList();
            
            private Search(){
                // nop
            }
            public static Search newInstance(){
                return new Search();
            }
        }
    }

}
