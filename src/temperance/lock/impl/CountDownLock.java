package temperance.lock.impl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import temperance.lock.ConditionLock;

public class CountDownLock implements ConditionLock {
    
    protected final int initial;
    
    protected final AtomicReference<CountDownLatch> lock = new AtomicReference<CountDownLatch>();
    
    public CountDownLock(final int initial){
        this.initial = initial;
        this.lock.set(new CountDownLatch(initial));
    }

    public void await() throws InterruptedException {
        CountDownLatch latch = lock.get();
        latch.await();
        synchronized(this){
            lock.set(new CountDownLatch(initial));
        }
    }

    public void release() {
        countDown();
    }
    
    public void countDown(){
        lock.get().countDown();
    }
    
    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder();
        buf.append("initial=").append(initial).append(",");
        buf.append("current=").append(lock.get().getCount());
        return buf.toString();
    }

}
