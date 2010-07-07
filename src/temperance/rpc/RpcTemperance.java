package temperance.rpc;

import temperance.exception.RpcException;

public interface RpcTemperance extends Rpc {

    public Response.Monitor monitor() throws RpcException;
    
    public static abstract class Response {
        public static class Monitor {
            public int inProgressTasks;
            public int queuedTasks;
            public long totalTasks;
            public long totalTime;
            
            private Monitor(){
                // nop
            }
            public static Monitor newInstance(){
                return new Monitor();
            }
        }
    }
}
