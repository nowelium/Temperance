package temperance;

import temperance.handler.Context;
import temperance.server.FullTextServer;
import temperance.server.Server;

public class ListServerStop {
    
    public static void main(String...args){
        StartStop st = new StartStop(){
            @Override
            protected void shutdown() {
                Server server = new FullTextServer(null, false, 17002);
                server.shutdown();
            }

            @Override
            protected void start(Context context, boolean daemonize) {
                // nop
            }
        };
        st.stop();
    }

}
