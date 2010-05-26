package temperance;

import temperance.handler.Context;
import temperance.server.MapServer;
import temperance.server.Server;

public class MapServerStart {
    
    public static void main(String...args){
        StartStop st = new StartStop(){
            @Override
            protected Server createServer(Context ctx, boolean daemonize, int port) {
                return new MapServer(ctx, daemonize, port);
            }
        };
        st.start(StartStop.createCliOptions(), args);
    }

}
