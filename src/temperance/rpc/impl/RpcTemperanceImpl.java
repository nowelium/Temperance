package temperance.rpc.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import temperance.core.Configure;
import temperance.core.Pooling;
import temperance.core.ThreadPool;
import temperance.exception.RpcException;
import temperance.rpc.RpcTemperance;

public class RpcTemperanceImpl implements RpcTemperance {
    
    protected static final Log logger = LogFactory.getLog(RpcTemperanceImpl.class);
    
    protected final Configure configure;
    
    protected final Pooling pooling;
    
    public RpcTemperanceImpl(Configure configure, Pooling pooling){
        this.configure = configure;
        this.pooling = pooling;
    }
    
    public Response.Monitor monitor() throws RpcException {
        final ThreadPool threadPool = pooling.getThreadPool();
        final Response.Monitor response = Response.Monitor.newInstance();
        response.inProgressTasks = threadPool.getInProgressTasks().size();
        response.queuedTasks = threadPool.getQueuedTasks().size();
        response.totalTasks = threadPool.getTotalTasks();
        response.totalTime = threadPool.getTotalTime();
        return response;
    }

}
