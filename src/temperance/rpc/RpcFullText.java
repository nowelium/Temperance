package temperance.rpc;

import java.util.List;

import temperance.exception.RpcException;
import temperance.util.Lists;

public interface RpcFullText extends Rpc {
    
    public Response.Add add(Request.Add request) throws RpcException;
    
    public Response.Delete delete(Request.Delete request) throws RpcException;
    
    public Response.DeleteByValue deleteByValue(Request.DeleteByValue request) throws RpcException;
    
    public Response.Search search(Request.Search request) throws RpcException;
    
    public Response.Reindex reindex(Request.Reindex request) throws RpcException;
    
    public static abstract class Request {
        public static enum Parser {
            MECAB(0),
            BIGRAM(1),
            PREFIX(2),
            HASH_CSV(3),
            HASH_TSV(4),
            HASH_SSV(5)
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
                case 3:
                    return HASH_CSV;
                case 4:
                    return HASH_TSV;
                case 5:
                    return HASH_SSV;
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
            public boolean asyncRequest = true;
            
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
            public boolean asyncRequest = true;
            
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
            public boolean asyncRequest = true;
            
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
        public static class Reindex {
            public String key;
            public boolean asyncRequest = true;
            
            private Reindex(){
                // nop
            }
            public static Reindex newInstance(){
                return new Reindex();
            }
        }
    }
    
    public static abstract class Response {
        public static enum Status {
            SUCCESS(0),
            ENQUEUE(1),
            
            FAILURE(10),
            TIMEOUT(11),
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
                    return SUCCESS;
                case 1:
                    return ENQUEUE;
                case 10:
                    return FAILURE;
                case 11:
                    return TIMEOUT;
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
        public static class Reindex {
            public Status status;
            
            private Reindex(){
                // nop
            }
            public static Reindex newInstance(){
                return new Reindex();
            }
        }
    }

}
