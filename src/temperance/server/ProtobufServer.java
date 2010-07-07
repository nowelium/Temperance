package temperance.server;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import temperance.core.Configure;
import temperance.protobuf.FullText.FullTextService;
import temperance.protobuf.List.ListService;
import temperance.protobuf.Map.MapService;
import temperance.protobuf.Mecab.MecabService;
import temperance.protobuf.Query.QueryService;
import temperance.protobuf.Temperance.TemperanceService;
import temperance.rpc.protobuf.ProtobufFullTextService;
import temperance.rpc.protobuf.ProtobufListService;
import temperance.rpc.protobuf.ProtobufMapService;
import temperance.rpc.protobuf.ProtobufMecabService;
import temperance.rpc.protobuf.ProtobufQueryService;
import temperance.rpc.protobuf.ProtobufTemperanceService;

import com.google.protobuf.BlockingService;
import com.googlecode.protobuf.socketrpc.SocketRpcServer;

public class ProtobufServer extends AbstractRpcServer {
    
    protected final SocketRpcServer server;
    
    public ProtobufServer(Configure configure, boolean daemonize, int rpcPort) {
        super(configure, ProtobufServer.class.getName(), daemonize);
        this.server = new SocketRpcServer(rpcPort, Executors.newCachedThreadPool());
    }

    @Override
    public void initServer(){
        for(BlockingService service: createBlockingService()){
            server.registerBlockingService(service);
        }
    }
    
    @Override
    public void startServer() {
        server.startServer();
    }

    @Override
    public void stopServer() {
        server.shutDown();
    }
    
    protected List<BlockingService> createBlockingService(){
        return Arrays.asList(
            FullTextService.newReflectiveBlockingService(new ProtobufFullTextService(createRpcFullText())),
            ListService.newReflectiveBlockingService(new ProtobufListService(createRpcList())),
            MapService.newReflectiveBlockingService(new ProtobufMapService(createRpcMap())),
            MecabService.newReflectiveBlockingService(new ProtobufMecabService(createRpcMecab())),
            QueryService.newReflectiveBlockingService(new ProtobufQueryService(createRpcQuery())),
            TemperanceService.newReflectiveBlockingService(new ProtobufTemperanceService(createRpcTemperance()))
        );
    }
}
