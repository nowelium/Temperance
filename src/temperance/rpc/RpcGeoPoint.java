package temperance.rpc;

import java.util.List;

import temperance.exception.RpcException;
import temperance.util.Lists;

public interface RpcGeoPoint {

    public Response.Add add(Request.Add request) throws RpcException;
    
    public Response.Get get(Request.Get request) throws RpcException;
    
    public Response.Count count(Request.Count request) throws RpcException;
    
    public Response.Search search(Request.Search request) throws RpcException;
    
    public static abstract class Request {
        public static class Point {
            public double latitude;
            public double longitude;
            
            private Point(){
                // nop
            }
            public static Point newInstance(){
                return new Point();
            }
        }
        public static class Add {
            public Point point;
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
            public Point point;
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
            public Point point;
            
            private Count(){
                // nop
            }
            public static Count newInstance(){
                return new Count();
            }
        }
        public static class Search {
            public Point point;
            public short precision;
            
            private Search(){
                // nop
            }
            public static Search newInstance(){
                return new Search();
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
