package temperance.lock.impl;

import java.util.concurrent.atomic.AtomicInteger;

import temperance.lock.ConditionLock;

public class CountDownLock implements ConditionLock {
    
    protected final int initial;
    
    protected final AtomicInteger counter;
    
    protected final CountedConditionLock.Sync sync = new CountedConditionLock.Sync();
    
    public CountDownLock(final int initial){
        this.initial = initial;
        this.counter = new AtomicInteger(initial);
        this.sync.update(initial);
    }

    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
        sync.update(initial);
    }

    public void release() {
        sync.releaseShared(1);
    }
    
    public void countDown(){
        sync.update(counter.decrementAndGet());
    }

}
