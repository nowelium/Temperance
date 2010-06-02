package temperance.lock.impl;

import java.util.concurrent.SynchronousQueue;

import temperance.lock.ConditionLock;

public class RendezvousLock implements ConditionLock {
    
    protected final Object condition = new Object();
    
    protected final SynchronousQueue<Object> sync = new SynchronousQueue<Object>();

    public void await() throws InterruptedException {
        sync.take();
    }

    public void release() {
        sync.offer(condition);
    }

}
