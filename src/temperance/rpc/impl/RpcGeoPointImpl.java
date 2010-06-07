package temperance.rpc.impl;

import temperance.exception.RpcException;
import temperance.rpc.Context;
import temperance.rpc.RpcGeoPoint;

public class RpcGeoPointImpl implements RpcGeoPoint {
    
    protected final Context context;
    
    public RpcGeoPointImpl(Context context){
        this.context = context;
    }

    public Response.Add add(Request.Add request) throws RpcException {
        return null;
    }

    public Response.Count count(Request.Count request) throws RpcException {
        return null;
    }

    public Response.Get get(Request.Get request) throws RpcException {
        return null;
    }

    public Response.Search search(Request.Search request) throws RpcException {
        return null;
    }

}
