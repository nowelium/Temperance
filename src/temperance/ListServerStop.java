package temperance;

import temperance.handler.Context;
import temperance.server.ListServer;
import temperance.server.Server;

public class ListServerStop {
    
    public static void main(String...args){
        StartStop st = new StartStop(){
            @Override
            protected Server createServer(Context ctx, boolean daemonize, int port) {
                return new ListServer(ctx, daemonize, port);
            }
        };
        st.stop();
    }

}
