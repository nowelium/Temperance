package temperance.rpc;

import java.util.List;

import temperance.exception.RpcException;
import temperance.util.Lists;

public interface RpcQuery extends Rpc {
    
    public Response.Select select(Request.Select request) throws RpcException;
    
    public Response.Delete delete(Request.Delete request) throws RpcException;
    
    public static abstract class Request {
        public static class Select {
            public String query;
            
            private Select(){
                // nop
            }
            public static Select newInstance(){
                return new Select();
            }
        }
        public static class Delete {
            public String query;
            
            private Delete(){
                // nop
            }
            public static Delete newInstance(){
                return new Delete();
            }
        }
    }
    
    public static abstract class Response {
        public static class Select {
            public List<String> values = Lists.newArrayList();
            
            private Select(){
                // nop
            }
            public static Select newInstance(){
                return new Select();
            }
        }
        public static class Delete {
            public List<String> values = Lists.newArrayList();
            
            private Delete(){
                // nop
            }
            public static Delete newInstance(){
                return new Delete();
            }
        }
    }

}
