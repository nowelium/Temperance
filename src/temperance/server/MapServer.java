package temperance.server;

import java.util.concurrent.Executors;

import temperance.handler.Context;
import temperance.handler.MapServiceHandler;
import temperance.protobuf.Map.MapService;

import com.googlecode.protobuf.socketrpc.SocketRpcServer;

public class MapServer extends AbstractDaemon {
    
    protected final SocketRpcServer server;
    
    protected final Context context;
    
    public MapServer(Context context, boolean daemonize, int rpcPort) {
        super(MapServer.class.getName(), daemonize);
        this.server = new SocketRpcServer(rpcPort, Executors.newCachedThreadPool());
        this.context = context;
    }

    @Override
    public void init() {
        server.registerBlockingService(MapService.newReflectiveBlockingService(new MapServiceHandler(context)));
    }

    @Override
    public void run() {
        server.startServer();
        try {
            Object o = new Object();
            synchronized (o) {
                o.wait();
            }
        } catch(InterruptedException e){
            //
        }
    }

    @Override
    public void stop() {
        server.shutDown();
    }
}
