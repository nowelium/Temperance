package temperance.server;

import static com.sun.akuma.CLibrary.LIBC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import com.sun.akuma.Daemon;

public abstract class AbstractDaemon implements Server {
    
    protected static final String LOG4J_RESOURCE_PATH;
    
    protected static final String PID_DIR;
    
    protected static final String PID_FILE_SUFFIX = ".pid";
    
    protected static final String ERR_FILE_SUFFIX = ".err";
    
    protected static final int SIGTERM = 15;
    
    static {
        PID_DIR = System.getProperty("temperance.pid.dir", "/tmp");
        LOG4J_RESOURCE_PATH = System.getProperty("temperance.log.path", "log4j.xml");
    }
    
    /**
     * :INT (中断:interrupt) C-c
     * <del>:HUP (切断: hangup)</del>
     * :TERM (停止: terminate)
     * :KILL (停止: kill)
     * の各シグナルに対する処理は全て停止としておく
     */
    protected final Signal[] shutdownSignals = {
        new Signal("INT"),
        new Signal("TERM"),
        //new Signal("KILL"),
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
                writer.write("\t");
                writer.write(stackTrace.toString());
                writer.write("\n");
            }
            writer.close();
        } catch(IOException ex){
            // nop
        }
    }
    
    public final void start(){
        if(pidFile.exists()){
            logger.info("pid file " + pidFile.getAbsolutePath() + " was already exists. stop it first.");
            System.exit(1);
        }
        
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
            //
            // signal handlers
            //
            Signal.handle(new Signal("HUP"), new ReloadSignal());
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
                
                logger.info("shuwdown signal(kill SIGTERM): " + pid);
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
    
    protected class ReloadSignal implements SignalHandler {
        public void handle(Signal signal){
            logger.info("reload signal: " + signal.getName() + " was occured.");
            
            logger.info("reload log configuration.");
            ClassLoader loader = AbstractDaemon.class.getClassLoader();
            URL log4jxml = loader.getResource(LOG4J_RESOURCE_PATH);
            if(null == log4jxml){
                logger.warn("log4j configration file not found: " + LOG4J_RESOURCE_PATH);
                return;
            }
            
            new DOMConfigurator().doConfigure(log4jxml, LogManager.getLoggerRepository());
        }
    }
    
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
