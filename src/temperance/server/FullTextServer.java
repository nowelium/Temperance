package temperance.server;

import java.util.concurrent.Executors;

import temperance.handler.Context;
import temperance.handler.FullTextServiceHandler;
import temperance.protobuf.FullText.FullTextService;

import com.googlecode.protobuf.socketrpc.SocketRpcServer;

public class FullTextServer extends AbstractDaemon {
    
    protected final SocketRpcServer server;
    
    protected final Context context;
    
    public FullTextServer(Context context, boolean daemonize, int rpcPort) {
        super(FullTextServer.class.getName(), daemonize);
        this.server = new SocketRpcServer(rpcPort, Executors.newCachedThreadPool());
        this.context = context;
    }

    @Override
    public void init() {
        server.registerBlockingService(FullTextService.newReflectiveBlockingService(new FullTextServiceHandler(context)));
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
