package temperance.rpc;

import temperance.exception.RpcException;

public interface RpcMap extends Rpc {
    
    public Response.Set set(Request.Set request) throws RpcException;
    
    public Response.Get get(Request.Get request) throws RpcException;
    
    public static abstract class Request {
        public static class Set {
            public static final int DEFAULT_EXPIRE = 86400;
            
            public String key;
            public String value;
            public int expire = DEFAULT_EXPIRE;
            
            private Set(){
                // nop
            }
            public static Set newInstance(){
                return new Set();
            }
        }
        public static class Get {
            public String key;
            
            private Get(){
                // nop
            }
            public static Get newInstance(){
                return new Get();
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
        public static class Get {
            public String value;
            
            private Get(){
                // nop
            }
            public static Get newInstance(){
                return new Get();
            }
        }
    }

}
