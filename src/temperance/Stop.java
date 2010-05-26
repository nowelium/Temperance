package temperance;

import temperance.handler.Context;
import temperance.server.Server;
import temperance.server.TemperanceServer;

public class Stop {
    public static void main(String...args){
        StartStop st = new StartStop(){
            @Override
            protected Server createServer(Context ctx, boolean daemonize, int port) {
                return new TemperanceServer(ctx, daemonize, port);
            }
        };
        st.stop();
    }
}
