package temperance.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import libmemcached.wrapper.MemcachedClient;
import libmemcached.wrapper.type.BehaviorType;

import org.junit.Test;

import temperance.hash.Digest;
import temperance.hashing.MecabHashing;


public class ConnectionPoolTest {
    
    @Test
    public void get(){
        Configure configure = new Configure();
        configure.setFullTextHashFunction(Digest.MD5);
        configure.setMemcached("localhost:11211");
        configure.setNodeFilter(MecabHashing.Filter.Nouns);
        configure.setPoolBehaviors(new HashMap<BehaviorType, Boolean>(){
            private static final long serialVersionUID = 1L;
            {
                put(BehaviorType.BUFFER_REQUESTS, Boolean.FALSE);
            }
        });
        configure.setInitialConnectionPoolSize(100);
        configure.setMaxConnectionPoolSize(200);
        
        Pooling pooling = new Pooling(configure);
        pooling.init();
        
        ConnectionPool pool = pooling.getConnectionPool();
        for(int i = 0; i < 5000; ++i){
            MemcachedClient c = pool.get();
            System.out.println(i);
            pool.release(c);
        }
        
        pooling.destroy();
    }
    
    @Test
    public void get_multi(){
        Configure configure = new Configure();
        configure.setFullTextHashFunction(Digest.MD5);
        configure.setMemcached("localhost:11211");
        configure.setNodeFilter(MecabHashing.Filter.Nouns);
        configure.setPoolBehaviors(new HashMap<BehaviorType, Boolean>(){
            private static final long serialVersionUID = 1L;
            {
                put(BehaviorType.BUFFER_REQUESTS, Boolean.FALSE);
            }
        });
        configure.setInitialConnectionPoolSize(100);
        configure.setMaxConnectionPoolSize(200);
        
        Pooling pooling = new Pooling(configure);
        pooling.init();
        
        final ConnectionPool pool = pooling.getConnectionPool();
        Thread th1 = new Thread(){
            @Override
            public void run(){
                for(int i = 0; i < 200; ++i){
                    MemcachedClient c = pool.get();
                    System.out.println("th1: " + i);
                    pool.release(c);
                }
            }
        };
        Thread th2 = new Thread(){
            @Override
            public void run(){
                for(int i = 0; i < 200; ++i){
                    MemcachedClient c = pool.get();
                    System.out.println("th2: " + i);
                    pool.release(c);
                }
            }
        };
        
        th1.start();
        th2.start();
        
        try {
            th1.join();
            th2.join();
        } catch(InterruptedException e){
        }
        
        pooling.destroy();
    }
    
    @Test
    public void get_multi_blocking_pool(){
        Configure configure = new Configure();
        configure.setFullTextHashFunction(Digest.MD5);
        configure.setMemcached("localhost:11211");
        configure.setNodeFilter(MecabHashing.Filter.Nouns);
        configure.setPoolBehaviors(new HashMap<BehaviorType, Boolean>(){
            private static final long serialVersionUID = 1L;
            {
                put(BehaviorType.BUFFER_REQUESTS, Boolean.FALSE);
            }
        });
        configure.setInitialConnectionPoolSize(100);
        configure.setMaxConnectionPoolSize(200);
        
        Pooling pooling = new Pooling(configure);
        pooling.init();
        
        final ConnectionPool pool = pooling.getConnectionPool();
        
        List<MemcachedClient> list = new ArrayList<MemcachedClient>();
        for(int i = 0; i < 200; ++i){
            MemcachedClient c = pool.get();
            list.add(c);
        }
        
        Thread th1 = new Thread(){
            @Override
            public void run(){
                List<MemcachedClient> list = new ArrayList<MemcachedClient>();
                for(int i = 0; i < 100; ++i){
                    MemcachedClient c = pool.get();
                    System.out.println("th1: " + i);
                    list.add(c);
                }
                for(MemcachedClient c: list){
                    pool.release(c);
                }
            }
        };
        Thread th2 = new Thread(){
            @Override
            public void run(){
                List<MemcachedClient> list = new ArrayList<MemcachedClient>();
                for(int i = 0; i < 100; ++i){
                    MemcachedClient c = pool.get();
                    System.out.println("th2: " + i);
                    list.add(c);
                }
                for(MemcachedClient c: list){
                    pool.release(c);
                }
            }
        };
        
        th1.start();
        th2.start();
        
        try {
            for(MemcachedClient c: list){
                System.out.println(c);
                pool.release(c);
                TimeUnit.MILLISECONDS.sleep(10);
            }
            
            th1.join();
            th2.join();
        } catch(InterruptedException e){
        }
        
        pooling.destroy();
    }

}
