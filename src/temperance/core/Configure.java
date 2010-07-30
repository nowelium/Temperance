package temperance.core;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import libmemcached.wrapper.type.BehaviorType;
import temperance.hash.HashFunction;
import temperance.hashing.MecabNodeFilter;

public class Configure {
    
    private String memcached;
    
    private String mecabrc;
    
    private HashFunction fullTextHashFunction;
    
    private MecabNodeFilter nodeFilter;
    
    private Map<BehaviorType, Boolean> poolBehaviors = new EnumMap<BehaviorType, Boolean>(BehaviorType.class);
    
    private int initialThreadPoolSize = 100;
    
    private int maxThreadPoolSize = 500;
    
    private long threadKeepAlive = 60L;
    
    private TimeUnit threadKeepAliveTimeUnit = TimeUnit.SECONDS;
    
    private int initialConnectionPoolSize = 100;
    
    private int maxConnectionPoolSize = 500;
    
    public String getMemcached(){
        return memcached;
    }
    
    public void setMemcached(String memcached){
        this.memcached = memcached;
    }

    public String getMecabrc(){
        return mecabrc;
    }
    
    public void setMecabrc(String mecabrc){
        this.mecabrc = mecabrc;
    }
    
    public HashFunction getFullTextHashFunction(){
        return fullTextHashFunction;
    }
    
    public void setFullTextHashFunction(HashFunction fullTextHashFunction){
        this.fullTextHashFunction = fullTextHashFunction;
    }

    public MecabNodeFilter getNodeFilter() {
        return nodeFilter;
    }

    public void setNodeFilter(MecabNodeFilter nodeFilter) {
        this.nodeFilter = nodeFilter;
    }

    public Map<BehaviorType, Boolean> getPoolBehaviors() {
        return poolBehaviors;
    }

    public void setPoolBehaviors(Map<BehaviorType, Boolean> poolBehaviors) {
        this.poolBehaviors = poolBehaviors;
    }

    public int getInitialThreadPoolSize() {
        return initialThreadPoolSize;
    }

    public void setInitialThreadPoolSize(int initialThreadPoolSize) {
        this.initialThreadPoolSize = initialThreadPoolSize;
    }

    public int getMaxThreadPoolSize() {
        return maxThreadPoolSize;
    }

    public void setMaxThreadPoolSize(int maxThreadPoolSize) {
        this.maxThreadPoolSize = maxThreadPoolSize;
    }

    public long getThreadKeepAlive() {
        return threadKeepAlive;
    }

    public void setThreadKeepAlive(long threadKeepAlive) {
        this.threadKeepAlive = threadKeepAlive;
    }

    public TimeUnit getThreadKeepAliveTimeUnit() {
        return threadKeepAliveTimeUnit;
    }

    public void setThreadKeepAliveTimeUnit(TimeUnit threadKeepAliveTimeUnit) {
        this.threadKeepAliveTimeUnit = threadKeepAliveTimeUnit;
    }

    public int getInitialConnectionPoolSize() {
        return initialConnectionPoolSize;
    }

    public void setInitialConnectionPoolSize(int initialConnectionPoolSize) {
        this.initialConnectionPoolSize = initialConnectionPoolSize;
    }

    public int getMaxConnectionPoolSize() {
        return maxConnectionPoolSize;
    }

    public void setMaxConnectionPoolSize(int maxConnectionPoolSize) {
        this.maxConnectionPoolSize = maxConnectionPoolSize;
    }

}
