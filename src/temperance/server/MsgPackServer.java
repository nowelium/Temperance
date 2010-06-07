package temperance.server;

import temperance.rpc.Context;

public class MsgPackServer extends AbstractRpcServer {

    protected MsgPackServer(Context context, boolean daemonize, int rpcPort) {
        super(context, MsgPackServer.class.getName(), daemonize);
    }

    @Override
    public void run() {
    }

    @Override
    public void stop() {
    }

}
