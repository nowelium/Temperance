package temperance;

import temperance.handler.Context;
import temperance.server.FullTextServer;
import temperance.server.ListServer;
import temperance.server.MapServer;
import temperance.server.Server;

public class Stop {
    
    public static void main(String...args){
        Context ctx = new Context();
        stop(new FullTextServer(ctx, false, 17001));
        stop(new ListServer(ctx, false, 17002));
        stop(new MapServer(ctx, false, 17003));
    }
    
    protected static void stop(Server s){
        s.shutdown();
    }
    
}
