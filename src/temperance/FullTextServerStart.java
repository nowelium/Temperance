package temperance;

import temperance.handler.Context;
import temperance.server.FullTextServer;
import temperance.server.Server;

public class FullTextServerStart {
    
    public static void main(String...args){
        StartStop st = new StartStop(){
            @Override
            protected Server createServer(Context ctx, boolean daemonize, int port) {
                return new FullTextServer(ctx, daemonize, port);
            }
        };
        st.start(StartStop.createCliOptions(), args);
    }

}
