package temperance.rpc;

import java.util.List;

import temperance.exception.RpcException;
import temperance.util.Lists;

public interface RpcFullText extends Rpc {
    
    public Response.Set set(Request.Set request) throws RpcException;
    
    public Response.Search search(Request.Search request) throws RpcException;
    
    public static abstract class Request {
        public static enum Parser {
            MECAB(0),
            BIGRAM(1),
            PREFIX(2),
            ;
            private final int value;
            private Parser(int value){
                this.value = value;
            }
            public int getValue(){
                return value;
            }
            public static Parser get(int num){
                switch(num){
                case 0:
                    return MECAB;
                case 1:
                    return BIGRAM;
                case 2:
                    return PREFIX;
                }
                throw new IllegalArgumentException("no such parser:" + num);
            }
        }
        public static class Set {
            public static final int DEFAULT_EXPIRE = 86400;
            
            public String key;
            public String str;
            public String value;
            public int expire = DEFAULT_EXPIRE;
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
