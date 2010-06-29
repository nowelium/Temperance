package temperance.rpc;

import java.util.List;

import temperance.exception.RpcException;
import temperance.util.Lists;

public interface RpcList extends Rpc {

    public Response.Add add(Request.Add request) throws RpcException;
    
    public Response.Get get(Request.Get request) throws RpcException;
    
    public Response.Count count(Request.Count request) throws RpcException;
    
    public Response.Delete delete(Request.Delete request) throws RpcException;
    
    public Response.DeleteByValue deleteByValue(Request.DeleteByValue request) throws RpcException;
    
    public Response.Reindex reindex(Request.Reindex request) throws RpcException;
    
    public static abstract class Request {
        public static class Add {
            public static final int DEFAULT_EXPIRE = 86400;
            
            public String key;
            public String value;
            public int expire = DEFAULT_EXPIRE;
            public boolean asyncRequest = true;
            
            private Add(){
                // nop
            }
            public static Add newInstance(){
                return new Add();
            }
        }
        public static class Get {
            public static final long DEFAULT_OFFSET = 0;
            public static final long DEFAULT_LIMIT = 1000;
            
            public String key;
            public long offset = DEFAULT_OFFSET;
            public long limit = DEFAULT_LIMIT;
            
            private Get(){
                // nop
            }
            public static Get newInstance(){
                return new Get();
            }
        }
        public static class Count {
            public String key;
            
            private Count(){
                // nop
            }
            public static Count newInstance(){
                return new Count();
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
        public static class Get {
            public List<String> values = Lists.newArrayList();
            
            private Get(){
                // nop
            }
            public static Get newInstance(){
                return new Get();
            }
        }
        public static class Count {
            public long count;
            
            private Count(){
                // nop
            }
            public static Count newInstance(){
                return new Count();
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
