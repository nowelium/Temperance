package temperance.server;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.msgpack.rpc.server.RPCRequestDecoder;
import org.msgpack.rpc.server.RPCResponseEncoder;
import org.msgpack.rpc.server.RPCServerHandler;
import org.msgpack.rpc.server.TCPServer;

import temperance.core.Configure;
import temperance.rpc.msgpack.MsgpackFullTextService;
import temperance.rpc.msgpack.MsgpackListService;
import temperance.rpc.msgpack.MsgpackMapService;
import temperance.rpc.msgpack.MsgpackMecabService;
import temperance.rpc.msgpack.MsgpackQueryService;

public class MsgPackServer extends AbstractRpcServer {
    
    protected final ServiceTcpServer server;
    
    public MsgPackServer(Configure configure, boolean daemonize, int rpcPort) {
        super(configure, MsgPackServer.class.getName() + "_" + rpcPort, daemonize);
        this.server = new ServiceTcpServer("0.0.0.0", rpcPort);
    }
    
    @Override
    public void initServer(){
        server.registerRpc(new MsgpackFullTextService(createRpcFullText()));
        server.registerRpc(new MsgpackListService(createRpcList()));
        server.registerRpc(new MsgpackMapService(createRpcMap()));
        server.registerRpc(new MsgpackMecabService(createRpcMecab()));
        server.registerRpc(new MsgpackQueryService(createRpcQuery()));
    }

    @Override
    public void startServer() {
        try {
            server.serv();
        } catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stopServer() {
        server.stop();
    }
    
    protected static class ServiceTcpServer extends TCPServer {
        protected static final Object nullHandler = new Object();
        protected final ServiceHandler serviceHandler = new ServiceHandler();
        public ServiceTcpServer(String host, int port) {
            this(new InetSocketAddress(host, port));
        }
        public ServiceTcpServer(InetSocketAddress addr) {
            super(addr, nullHandler);
            bootstrap.setPipelineFactory(new ServicePipelineFactory(serviceHandler, true));
        }
        public void registerRpc(Object handler){
            serviceHandler.registerRpc(handler);
        }
    }
    
    protected static class ServicePipelineFactory implements ChannelPipelineFactory {
        protected final ServiceHandler handler;
        protected final RPCResponseEncoder encoder;
        protected final boolean isStream;
        public ServicePipelineFactory(ServiceHandler handler, boolean isStream){
            this.handler = handler;
            this.encoder = new RPCResponseEncoder();
            this.isStream = isStream;
        }
        public ChannelPipeline getPipeline() throws Exception {
            ChannelPipeline pipeline = Channels.pipeline();
            pipeline.addLast("decoder", new RPCRequestDecoder(isStream));
            pipeline.addLast("encoder", encoder);
            pipeline.addLast("handler", handler);
            return pipeline;
        }
    }
    
    protected static class ServiceHandler extends RPCServerHandler {
        protected static final char PACKAGE_DELIM = '#';
        protected static final Object nullHandler = new Object();
        protected final Map<String, Object> handlers = new HashMap<String, Object>();
        protected final Map<Object, Method[]> methods = new HashMap<Object, Method[]>();
        public ServiceHandler(){
            super(nullHandler);
        }
        public void registerRpc(Object handler){
            Class<?> rpcClass = handler.getClass();
            String className = rpcClass.getName();
            for(Method method: rpcClass.getDeclaredMethods()){
                handlers.put(className + PACKAGE_DELIM + method.getName(), handler);
            }
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected Object callMethod(Object nullHandler, String method, AbstractList params) throws Exception {
            Object handler = handlers.get(method);
            if (handler == null) {
                throw new IOException("No such method");
            }
            String methodName = method.substring(method.indexOf(PACKAGE_DELIM) + 1);
            Method m = findMethod(handler, methodName, params);
            Object[] args = convertArgs(params, m);
            return m.invoke(handler, args);
        }
        
        protected static Object[] convertArgs(List<?> args, Method m){
            Object[] returnValue = new Object[args.size()];
            Class<?>[] types = m.getParameterTypes();
            for(int i = 0; i < types.length; ++i){
                Class<?> type = types[i];
                Object arg = args.get(i);
                if(null == arg){
                    returnValue[i] = null;
                    continue;
                }
                
                if(type.equals(long.class)){
                    returnValue[i] = convertPrimitiveLong(arg);
                    continue;
                }
                if(type.equals(int.class)){
                    returnValue[i] = convertPrimitiveInt(arg);
                    continue;
                }
                if(type.equals(boolean.class)){
                    returnValue[i] = convertPrimitiveBoolean(arg);
                }
                if(type.equals(String.class)){
                    returnValue[i] = convertString(arg);
                    continue;
                }
            }
            return returnValue;
        }
        protected static int convertPrimitiveInt(Object obj){
            if(obj instanceof Number){
                return ((Number) obj).intValue();
            }
            
            Class<?> c = obj.getClass();
            if(String.class.equals(c)){
                return Integer.parseInt(((String) obj));
            }
            return Integer.TYPE.cast(obj);
        }
        protected static long convertPrimitiveLong(Object obj){
            if(obj instanceof Number){
                return ((Number) obj).longValue();
            }
            Class<?> c = obj.getClass();
            if(String.class.equals(c)){
                return Long.parseLong((String) obj);
            }
            return Long.TYPE.cast(obj);
        }
        protected static boolean convertPrimitiveBoolean(Object obj){
            if(obj instanceof Boolean){
                return ((Boolean) obj).booleanValue();
            }
            Class<?> c = obj.getClass();
            if(String.class.equals(c)){
                return Boolean.parseBoolean((String) obj);
            }
            return Boolean.TYPE.cast(obj);
        }
        protected static String convertString(Object obj){
            Class<?> c = obj.getClass();
            if(byte[].class.equals(c)){
                return new String((byte[]) obj);
            }
            return obj.toString();
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected Method findMethod(Object handler, String method, AbstractList params) {
            Method[] ms = methods.get(handler);
            if(ms == null){
                ms = handler.getClass().getDeclaredMethods();
                methods.put(handler, ms);
            }
            
            int nParams = params.size();
            for (int i = 0; i < ms.length; i++) {
                Method m = ms[i];
                if (!method.equals(m.getName())) continue;
                if (nParams != m.getParameterTypes().length) continue;
                return m;
            }
            return null;
        }
    }

}
