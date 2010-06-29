package temperance.server;

import static com.sun.akuma.CLibrary.LIBC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import com.sun.akuma.Daemon;

public abstract class AbstractDaemon implements Server {
    
    protected static final String PID_DIR;
    
    protected static final String PID_FILE_SUFFIX = ".pid";
    
    protected static final String ERR_FILE_SUFFIX = ".err";
    
    protected static final int SIGTERM = 15;
    
    static {
        PID_DIR = System.getProperty("temperance.pid.dir", "/tmp");
    }
    
    /**
     * :INT (中断:interrupt) C-c
     * :HUP (切断: hangup)
     * :TERM (停止: terminate)
     * :KILL (停止: kill)
     * の各シグナルに対する処理は全て停止としておく
     */
    protected final Signal[] shutdownSignals = {
        new Signal("INT"),
        new Signal("HUP"),
        new Signal("TERM"),
        new Signal("KILL"),
    };
    
    protected final String name;
    
    protected final boolean daemonize;
    
    protected final Log logger;
    
    protected final File pidFile;
    
    protected final File errFile;
    
    protected AbstractDaemon(final String name, boolean daemonize){
        this.name = name;
        this.daemonize = daemonize;
        this.logger = LogFactory.getLog(name);
        this.pidFile = new File(PID_DIR + "/" + name + PID_FILE_SUFFIX);
        this.errFile = new File(PID_DIR + "/" + name + ERR_FILE_SUFFIX);
    }
    
    protected void logError(Throwable t){
        t.printStackTrace(System.err);
        if(logger.isWarnEnabled()){
            logger.warn(t.getMessage(), t);
        }
        
        try {
            FileWriter writer = new FileWriter(errFile);
            writer.write(t.getMessage());
            writer.write("\n");
            for(StackTraceElement stackTrace: t.getStackTrace()){
                writer.write(stackTrace.toString());
                writer.write("\n");
            }
            writer.close();
        } catch(IOException ex){
            // nop
        }
    }
    
    public final void start(){
        try {
            Daemon daemon = new Daemon();
            if(daemon.isDaemonized()){
                logger.debug("daemon init: " + pidFile.getAbsolutePath());
                daemon.init(pidFile.getAbsolutePath());
            } else {
                if(daemonize){
                    daemon.daemonize();
                    System.exit(0);
                }
            }
            logger.info("starting process(" + LIBC.getpid() + ":" + name + ")");
            for(Signal signal: shutdownSignals){
                Signal.handle(signal, new ShutdownSignal());
            }
            
            logger.info("init process");
            init();
            
            logger.info("run process");
            run();
        } catch(Exception e){
            logError(e);
        } catch(Throwable t){
            logError(t);
        }
    }
    
    public final void shutdown(){
        try {
            logger.debug("read pid file: " + pidFile);
            
            FileReader reader = new FileReader(pidFile);
            try {
                BufferedReader in = new BufferedReader(reader);
                int pid = Integer.valueOf(in.readLine());
                
                logger.info("shuwdown(kill SIGTERM): " + pid);
                LIBC.kill(pid, SIGTERM);
            } finally {
                reader.close();
            }
        } catch(Exception e){
            logError(e);
        } catch(Throwable t){
            logError(t);
        }
    }
    
    public abstract void init();
    
    public abstract void run();
    
    public abstract void stop();
    
    protected class ShutdownSignal implements SignalHandler {
        public void handle(Signal signal) {
            logger.info("shutdown signal: " + signal.getName() + " was occured.");
            
            logger.info("exiting process(" + LIBC.getpid() + ":" + name + ")");
            stop();
            handleShutdown();
            if(daemonize){
                LIBC.kill(0, SIGTERM);
            }
            
            logger.info("process exited.");
            System.exit(0);
        }
        protected void handleShutdown(){
            pidFile.delete();
            errFile.delete();
        }
    }
}
