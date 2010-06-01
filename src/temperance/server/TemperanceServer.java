package temperance.server;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import temperance.handler.Context;
import temperance.handler.FullTextServiceHandler;
import temperance.handler.ListServiceHandler;
import temperance.handler.MapServiceHandler;
import temperance.handler.MecabServiceHandler;
import temperance.handler.QueryServiceHandler;
import temperance.memcached.Pool;
import temperance.protobuf.FullText.FullTextService;
import temperance.protobuf.List.ListService;
import temperance.protobuf.Map.MapService;
import temperance.protobuf.Mecab.MecabService;
import temperance.protobuf.Query.QueryService;

import com.google.protobuf.BlockingService;
import com.googlecode.protobuf.socketrpc.SocketRpcServer;

public class TemperanceServer extends AbstractDaemon {
    
    protected final SocketRpcServer server;
    
    protected final Context context;
    
    public TemperanceServer(Context context, boolean daemonize, int rpcPort) {
        super(TemperanceServer.class.getName(), daemonize);
        this.server = new SocketRpcServer(rpcPort, Executors.newCachedThreadPool());
        this.context = context;
    }

    @Override
    public void init() {
        final Pool pool = new Pool(context);
        pool.init();
        
        final List<BlockingService> services = createBlockingService(context, pool);
        for(BlockingService service: services){
            server.registerBlockingService(service);
        }
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
    
    protected List<BlockingService> createBlockingService(Context context, Pool pool){
        return Arrays.asList(
            FullTextService.newReflectiveBlockingService(new FullTextServiceHandler(context, pool)),
            ListService.newReflectiveBlockingService(new ListServiceHandler(context, pool)),
            MapService.newReflectiveBlockingService(new MapServiceHandler(context, pool)),
            QueryService.newReflectiveBlockingService(new QueryServiceHandler(context, pool)),
            MecabService.newReflectiveBlockingService(new MecabServiceHandler(context))
        );
    }
}
