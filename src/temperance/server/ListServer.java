package temperance.server;

import java.util.concurrent.Executors;

import temperance.handler.Context;
import temperance.handler.ListServiceHandler;
import temperance.protobuf.List.ListService;

import com.googlecode.protobuf.socketrpc.SocketRpcServer;

public class ListServer extends AbstractDaemon {
    
    protected final SocketRpcServer server;
    
    protected final Context context;
    
    public ListServer(Context context, boolean daemonize, int rpcPort) {
        super(ListServer.class.getName(), daemonize);
        this.server = new SocketRpcServer(rpcPort, Executors.newCachedThreadPool());
        this.context = context;
    }

    @Override
    public void init() {
        server.registerBlockingService(ListService.newReflectiveBlockingService(new ListServiceHandler(context)));
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
