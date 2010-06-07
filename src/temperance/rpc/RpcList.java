package temperance.rpc;

import java.util.List;

import temperance.exception.RpcException;
import temperance.util.Lists;

public interface RpcList {

    public Response.Add add(Request.Add request) throws RpcException;
    
    public Response.Get get(Request.Get request) throws RpcException;
    
    public Response.Count count(Request.Count request) throws RpcException;
    
    public static abstract class Request {
        public static class Add {
            public String key;
            public String value;
            public int expire = 86400;
            
            private Add(){
                // nop
            }
            public static Add newInstance(){
                return new Add();
            }
        }
        public static class Get {
            public String key;
            public long offset = 0;
            public long limit = 1000;
            
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
    }
}
