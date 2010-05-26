package temperance;

import temperance.handler.Context;
import temperance.server.ListServer;
import temperance.server.Server;

public class MapServerStart {
    
    public static void main(String...args){
        StartStop st = new StartStop(){
            @Override
            protected void shutdown() {
                // nop
            }

            @Override
            protected void start(Context context, boolean daemonize) {
                Server server = new ListServer(null, false, 17003);
                server.start();
            }
        };
        st.start(StartStop.createCliOptions(), args);
    }

}
