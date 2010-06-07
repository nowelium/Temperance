package temperance.server;

import temperance.memcached.ConnectionPool;
import temperance.rpc.Context;
import temperance.rpc.RpcFullText;
import temperance.rpc.RpcGeoPoint;
import temperance.rpc.RpcList;
import temperance.rpc.RpcMap;
import temperance.rpc.RpcMecab;
import temperance.rpc.RpcQuery;
import temperance.rpc.impl.RpcFullTextImpl;
import temperance.rpc.impl.RpcGeoPointImpl;
import temperance.rpc.impl.RpcListImpl;
import temperance.rpc.impl.RpcMapImpl;
import temperance.rpc.impl.RpcMecabImpl;
import temperance.rpc.impl.RpcQueryImpl;

public abstract class AbstractRpcServer extends AbstractDaemon {

    protected final Context context;
    
    protected final ConnectionPool pool;
    
    protected AbstractRpcServer(Context context, String serverName, boolean daemonize) {
        super(serverName, daemonize);
        this.context = context;
        this.pool = new ConnectionPool(context);
    }
    
    @Override
    public final void init() {
        pool.init();
    }
    
    protected RpcFullText createRpcFullText(){
        return new RpcFullTextImpl(context, pool);
    }
    
    protected RpcGeoPoint createRpcGeoPoint(){
        return new RpcGeoPointImpl(context);
    }
    
    protected RpcList createRpcList(){
        return new RpcListImpl(context, pool);
    }
    
    protected RpcMap createRpcMap(){
        return new RpcMapImpl(context, pool);
    }
    
    protected RpcMecab createRpcMecab(){
        return new RpcMecabImpl(context);
    }
    
    protected RpcQuery createRpcQuery(){
        return new RpcQueryImpl(context, pool);
    }
    
}
