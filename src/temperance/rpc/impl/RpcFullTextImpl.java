package temperance.rpc.impl;

import temperance.handler.Context;
import temperance.rpc.RpcFullText;

public class RpcFullTextImpl implements RpcFullText {

    protected final Context context;
    
    public RpcFullTextImpl(Context context){
        this.context = context;
    }
    
    public Response.Search search(Request.Search request) {
        return null;
    }

    public Response.Set set(Request.Set request) {
        return null;
    }

}
