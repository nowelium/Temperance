package temperance.rpc;

import java.util.List;

import temperance.exception.RpcException;
import temperance.util.Lists;

public interface RpcMecab {
    
    public Response.Parse parse(Request.Parse request) throws RpcException;
    
    public static abstract class Request {
        public static class Parse {
            public String str;
            
            private Parse(){
                // nop
            }
            public static Parse newInstance(){
                return new Parse();
            }
        }
    }
    
    public static abstract class Response {
        public static class Node {
            public String surface;
            public String feature;
            
            private Node(){
                // nop
            }
            public static Node newInstance(){
                return new Node();
            }
        }
        public static class Parse {
            public List<Node> nodes = Lists.newArrayList();
            
            private Parse(){
                // nop
            }
            public static Parse newInstance(){
                return new Parse();
            }
        }
    }

}
