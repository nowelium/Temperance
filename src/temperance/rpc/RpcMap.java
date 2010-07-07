package temperance.rpc;

import java.util.List;

import temperance.exception.RpcException;
import temperance.util.Lists;

public interface RpcMap extends Rpc {
    
    public Response.Set set(Request.Set request) throws RpcException;
    
    public Response.Get get(Request.Get request) throws RpcException;
    
    public Response.GetValues getValues(Request.GetValues request) throws RpcException;
    
    public Response.Delete delete(Request.Delete request) throws RpcException;
    
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
        public static class GetValues {
            public List<String> keys = Lists.newArrayList();
            
            private GetValues(){
                // nop
            }
            public static GetValues newInstance(){
                return new GetValues();
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
        public static class Entry {
            public String key;
            public String value;
            
            private Entry(){
                // nop
            }
            public static Entry newInstance(){
                return new Entry();
            }
        }
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
        public static class GetValues {
            public List<Entry> values = Lists.newArrayList();
            
            public void add(String key, String value){
                Entry e = Entry.newInstance();
                e.key = key;
                e.value = value;
                values.add(e);
            }
            
            private GetValues(){
                // nop
            }
            public static GetValues newInstance(){
                return new GetValues();
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
