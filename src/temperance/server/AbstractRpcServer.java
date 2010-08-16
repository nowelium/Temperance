package temperance.server;

import temperance.core.Configure;
import temperance.core.Pooling;
import temperance.exception.InitializationException;
import temperance.rpc.RpcFullText;
import temperance.rpc.RpcGeoPoint;
import temperance.rpc.RpcList;
import temperance.rpc.RpcMap;
import temperance.rpc.RpcMecab;
import temperance.rpc.RpcQuery;
import temperance.rpc.RpcQueue;
import temperance.rpc.RpcTemperance;
import temperance.rpc.impl.RpcFullTextImpl;
import temperance.rpc.impl.RpcGeoPointImpl;
import temperance.rpc.impl.RpcListImpl;
import temperance.rpc.impl.RpcMapImpl;
import temperance.rpc.impl.RpcMecabImpl;
import temperance.rpc.impl.RpcQueryImpl;
import temperance.rpc.impl.RpcQueueImpl;
import temperance.rpc.impl.RpcTemperanceImpl;

import com.sun.jna.Native;

public abstract class AbstractRpcServer extends AbstractDaemon {

    protected final Configure configure;
    
    protected final Pooling pooling;
    
    protected AbstractRpcServer(Configure configure, String serverName, boolean daemonize) {
        super(serverName, daemonize);
        this.configure = configure;
        this.pooling = new Pooling(configure);
        
        if(logger.isDebugEnabled()){
            logger.debug("Native.isProtected: " + Native.isProtected());
        }
    }
    
    /**
     * initialize server thread
     */
    protected abstract void initServer();
    
    /**
     * start server thread
     */
    protected abstract void startServer();
    
    /**
     * stop server thread
     */
    protected abstract void stopServer();
    
    @Override
    public final void init() {
        logger.info("pool init");
        try {
            pooling.init();
        } catch(InitializationException e){
            logError(e);
            stop();
            System.exit(1);
        }
        
        logger.info("server init");
        initServer();
    }
    
    /**
     * start server thread and stand-by until #stop called
     */
    @Override
    public final void run(){
        logger.info("server start");
        startServer();
        
        logger.info("running...");
        try {
            Object o = new Object();
            synchronized (o) {
                o.wait();
            }
        } catch(InterruptedException e){
            //
        }
    }
    
    /**
     * サーバスレッドの停止を呼び出します
     */
    @Override
    public final void stop() {
        logger.info("stop server");
        stopServer();
        
        logger.info("stop pool");
        pooling.destroy();
    }
    
    protected RpcFullText createRpcFullText(){
        return new RpcFullTextImpl(configure, pooling);
    }
    
    protected RpcGeoPoint createRpcGeoPoint(){
        return new RpcGeoPointImpl(configure);
    }
    
    protected RpcList createRpcList(){
        return new RpcListImpl(configure, pooling);
    }
    
    protected RpcMap createRpcMap(){
        return new RpcMapImpl(configure, pooling);
    }
    
    protected RpcMecab createRpcMecab(){
        return new RpcMecabImpl(configure);
    }
    
    protected RpcQuery createRpcQuery(){
        return new RpcQueryImpl(configure, pooling);
    }
    
    protected RpcQueue createRpcQueue(){
        return new RpcQueueImpl(configure, pooling);
    }
    
    protected RpcTemperance createRpcTemperance(){
        return new RpcTemperanceImpl(configure, pooling);
    }
    
}
