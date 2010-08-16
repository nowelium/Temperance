package temperance.rpc;

import java.util.concurrent.TimeUnit;

import temperance.exception.RpcException;

public interface RpcQueue extends Rpc {
    
    public Response.Enqueue enqueue(Request.Enqueue request) throws RpcException;
    
    public Response.Dequeue dequeue(Request.Dequeue request) throws RpcException;
    
    public static abstract class Request {
        public static class Enqueue {
            public static final int DEFAULT_EXPIRE = 86400;
            
            public String key;
            public String value;
            public int expire = DEFAULT_EXPIRE;
            
            private Enqueue(){
                // nop
            }
            public static Enqueue newInstance(){
                return new Enqueue();
            }
        }
        public static class Dequeue {
            public static final int DEFAULT_TIMEOUT = 10;
            public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;
            
            public String key;
            public int timeout = DEFAULT_TIMEOUT;
            public TimeUnit unit = DEFAULT_TIME_UNIT;
            
            private Dequeue(){
                // nop
            }
            public static Dequeue newInstance(){
                return new Dequeue();
            }
        }
    }
    public static abstract class Response {
        public static class Enqueue {
            public boolean succeed;
            
            private Enqueue(){
                // nop
            }
            public static Enqueue newInstance(){
                return new Enqueue();
            }
        }
        public static class Dequeue {
            public String value;
            
            private Dequeue(){
                // nop
            }
            public static Dequeue newInstance(){
                return new Dequeue();
            }
        }
    }

}
