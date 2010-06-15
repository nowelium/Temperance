package temperance.rpc;

import java.util.List;

import temperance.exception.RpcException;
import temperance.util.Lists;

public interface RpcList extends Rpc {

    public Response.Add add(Request.Add request) throws RpcException;
    
    public Response.Get get(Request.Get request) throws RpcException;
    
    public Response.Count count(Request.Count request) throws RpcException;
    
    public Response.Delete delete(Request.Delete request) throws RpcException;
    
    public static abstract class Request {
        public static class Add {
            public static final int DEFAULT_EXPIRE = 86400;
            
            public String key;
            public String value;
            public int expire = DEFAULT_EXPIRE;
            
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
            
            private Delete(){
                // nop
            }
            public static Delete newInstance(){
                return new Delete();
            }
        }
    }
    public static abstract class Response {
        public static class Add {
            public boolean succeed;
            
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
            public boolean succeed;
            
            private Delete(){
                // nop
            }
            public static Delete newInstance(){
                return new Delete();
            }
        }
    }
}
