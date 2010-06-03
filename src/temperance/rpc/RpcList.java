package temperance.rpc;

import java.util.List;

public interface RpcList {

    public Response.Add add(Request.Add request);
    
    public Response.Get get(Request.Get request);
    
    public Response.Count count(Request.Count request);
    
    public static interface Request {
        public static class Add {
            public String key;
            public String value;
            public int expire = 86400;
        }
        public static class Get {
            public String key;
            public long offset = 0;
            public long limit = 1000;
        }
        public static class Count {
            public String key;
        }
    }
    public static interface Response {
        public static class Add {
            public boolean succeed;
        }
        public static class Get {
            public List<String> values;
        }
        public static class Count {
            public long count;
        }
    }
}
