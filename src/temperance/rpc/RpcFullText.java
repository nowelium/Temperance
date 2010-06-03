package temperance.rpc;

import java.util.List;

public interface RpcFullText {
    
    public Response.Set set(Request.Set request);
    
    public Response.Search search(Request.Search request);
    
    public static interface Request {
        public static enum Parser {
            MECAB,
            BIGRAM,
            PREFIX
        }
        public static class Set {
            public String key;
            public String str;
            public String value;
            public int expire = 86400;
            public Parser parser = Parser.MECAB;
        }
        public static class Search {
            public String key;
            public String str;
            public Parser parser = Parser.MECAB;
        }
    }
    
    public static interface Response {
        public static class Set {
            public boolean succeed;
        }
        public static class Search {
            public List<String> values;
        }
    }

}
