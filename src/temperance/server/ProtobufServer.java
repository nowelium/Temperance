package temperance.server;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import temperance.protobuf.FullText.FullTextService;
import temperance.protobuf.List.ListService;
import temperance.protobuf.Map.MapService;
import temperance.protobuf.Mecab.MecabService;
import temperance.protobuf.Query.QueryService;
import temperance.rpc.Context;
import temperance.rpc.protobuf.ProtobufFullTextService;
import temperance.rpc.protobuf.ProtobufListService;
import temperance.rpc.protobuf.ProtobufMapService;
import temperance.rpc.protobuf.ProtobufMecabService;
import temperance.rpc.protobuf.ProtobufQueryService;

import com.google.protobuf.BlockingService;
import com.googlecode.protobuf.socketrpc.SocketRpcServer;

public class ProtobufServer extends AbstractRpcServer {
    
    protected final SocketRpcServer server;
    
    public ProtobufServer(Context context, boolean daemonize, int rpcPort) {
        super(context, ProtobufServer.class.getName(), daemonize);
        this.server = new SocketRpcServer(rpcPort, Executors.newCachedThreadPool());
    }

    @Override
    public void run() {
        for(BlockingService service: createBlockingService()){
            server.registerBlockingService(service);
        }

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
    
    protected List<BlockingService> createBlockingService(){
        return Arrays.asList(
            FullTextService.newReflectiveBlockingService(new ProtobufFullTextService(createRpcFullText())),
            ListService.newReflectiveBlockingService(new ProtobufListService(createRpcList())),
            MapService.newReflectiveBlockingService(new ProtobufMapService(createRpcMap())),
            MecabService.newReflectiveBlockingService(new ProtobufMecabService(createRpcMecab())),
            QueryService.newReflectiveBlockingService(new ProtobufQueryService(createRpcQuery()))
        );
    }
}
