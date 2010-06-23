package temperance.rpc;

import java.util.List;

import temperance.exception.RpcException;
import temperance.util.Lists;

public interface RpcFullText extends Rpc {
    
    public Response.Add add(Request.Add request) throws RpcException;
    
    public Response.Delete delete(Request.Delete request) throws RpcException;
    
    public Response.DeleteByValue deleteByValue(Request.DeleteByValue request) throws RpcException;
    
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
        public static class Add {
            public static final int DEFAULT_EXPIRE = 86400;
            
            public String key;
            public String str;
            public String value;
            public int expire = DEFAULT_EXPIRE;
            public Parser parser = Parser.MECAB;
            
            private Add(){
                // nop
            }
            public static Add newInstance(){
                return new Add();
            }
        }
        public static class Delete {
            public static final int DEFAULT_EXPIRE = 0;
            
            public String key;
            public int expire = DEFAULT_EXPIRE;
            
            private Delete(){
                // nop
            }
            public static Delete newInstance(){
                return new Delete();
            }
        }
        public static class DeleteByValue {
            public static final int DEFAULT_EXPIRE = 0;
            
            public String key;
            public String value;
            public int expire = DEFAULT_EXPIRE;
            
            private DeleteByValue(){
                // nop
            }
            public static DeleteByValue newInstance(){
                return new DeleteByValue();
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
        public static enum Status {
            FAILURE(0),
            SUCCESS(1),
            ENQUEUE(2),
            ;
            
            private final int value;
            private Status(int value){
                this.value = value;
            }
            public int getValue(){
                return value;
            }
            public static Status get(int num){
                switch(num){
                case 0:
                    return FAILURE;
                case 1:
                    return SUCCESS;
                case 2:
                    return ENQUEUE;
                }
                throw new IllegalArgumentException("no such status:" + num);
            }
        }
        public static class Add {
            public Status status;
            
            private Add(){
                // nop
            }
            public static Add newInstance(){
                return new Add();
            }
        }
        public static class Delete {
            public Status status;
            
            private Delete(){
                // nop
            }
            public static Delete newInstance(){
                return new Delete();
            }
        }
        public static class DeleteByValue {
            public Status status;
            
            private DeleteByValue(){
                // nop
            }
            public static DeleteByValue newInstance(){
                return new DeleteByValue();
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
