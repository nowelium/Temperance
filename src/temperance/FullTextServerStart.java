package temperance;

import temperance.handler.Context;
import temperance.server.FullTextServer;
import temperance.server.Server;

public class FullTextServerStart {
    
    public static void main(String...args){
        StartStop st = new StartStop(){
            @Override
            protected void shutdown() {
                // nop
            }

            @Override
            protected void start(Context context, boolean daemonize) {
                Server server = new FullTextServer(null, false, 17001);
                server.start();
            }
        };
        st.start(StartStop.createCliOptions(), args);
    }

}
