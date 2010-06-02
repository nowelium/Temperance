package temperance.lock;

public interface ConditionLock {
    public void await() throws InterruptedException;

    public void release();
}
