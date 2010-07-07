package temperance;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;

import temperance.protobuf.Temperance;
import temperance.protobuf.Temperance.TemperanceService;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import com.googlecode.protobuf.socketrpc.RpcChannels;
import com.googlecode.protobuf.socketrpc.SocketRpcConnectionFactory;
import com.googlecode.protobuf.socketrpc.SocketRpcController;

public class Monitor {
    
    public static void main(String...args){
        Option optHost = new Option("th", "target_host", true, "taget host");
        optHost.setRequired(true);
        Option optPort = new Option("tp", "target_port", true, "target port");
        optPort.setRequired(true);
        Option optInterval = new Option("interval", "interval", true, "refresh interval(default 3.0)");
        
        Options options = new Options();
        options.addOption(optHost);
        options.addOption(optPort);
        options.addOption(optInterval);
        
        try {
            Parser parser = new GnuParser();
            CommandLine cli = parser.parse(options, args, true);
            
            String host = cli.getOptionValue("th", "localhost");
            String port = cli.getOptionValue("tp", "17001");
            String interval = cli.getOptionValue("interval", "3.0");
            
            monitor(host, Integer.parseInt(port), Double.parseDouble(interval));
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(Monitor.class.getSimpleName(), options);
            System.exit(0);
        }
    }
    
    protected static void monitor(String host, int port, double interval){
        final SocketRpcConnectionFactory factory = new SocketRpcConnectionFactory(host, port);
        final BlockingRpcChannel channel = RpcChannels.newBlockingRpcChannel(factory);
        final TemperanceService.BlockingInterface service = TemperanceService.newBlockingStub(channel);
        
        final long intervalMillis = Math.round(interval * 1000);
        try {
            System.out.print("\033[2J"); // clear screen
            while(true){
                System.out.print("\033[0;0H"); // location(0, 0)
                
                final RpcController controller = new SocketRpcController();
                
                Temperance.Request.Monitor request = Temperance.Request.Monitor.newBuilder().build();
                try {
                    Temperance.Response.Monitor response = service.monitor(controller, request);
                    
                    int inProgressTasks = response.getInProgressTasks();
                    int queuedTasks = response.getQueuedTasks();
                    long totalTasks = response.getTotalTasks();
                    long totalTime = response.getTotalTime();
                    double averateTime = 0.0;
                    if(1 < totalTasks){
                        averateTime = totalTime / totalTasks;
                    }
                    
                    StringBuilder buf = new StringBuilder();
                    buf.append("host(").append(host).append(")").append("\t");
                    buf.append("port(").append(port).append(")").append("\t");
                    buf.append("interval(").append(interval).append(")").append("\t");
                    buf.append("\n");
                    
                    buf.append("------------------------").append("\n");
                    
                    buf.append("\t").append(String.format("%12s", "inProgress"));
                    buf.append("\t").append(String.format("%10d tasks", inProgressTasks));
                    buf.append("\n");
                    buf.append("\t").append(String.format("%12s", "queued"));
                    buf.append("\t").append(String.format("%10d tasks", queuedTasks));
                    buf.append("\n");
                    buf.append("\t").append(String.format("%12s", "total"));
                    buf.append("\t").append(String.format("%10d tasks", totalTasks));
                    buf.append("\n");
                    buf.append("\t").append(String.format("%12s", "totalExec"));
                    buf.append("\t").append(String.format("%10d ms", totalTime));
                    buf.append("\n");
                    buf.append("\t").append(String.format("%12s", "average"));
                    buf.append("\t").append(String.format("%10.1f ms", averateTime));
                    buf.append("\n");
                    
                    buf.append("------------------------").append("\n");
                    buf.append(new Date());
                    
                    System.out.print(buf.toString());
                } catch(ServiceException e){
                    System.out.print("Error: " + e.getMessage());
                }
                System.out.print("\n");
                System.out.flush();
                System.out.print("\033[1K"); // clear left
                // System.out.print("\033[2K"); // clear line
                
                try {
                    TimeUnit.MILLISECONDS.sleep(intervalMillis);
                } catch(InterruptedException e){
                    throw new RuntimeException(e);
                }
            }
        } finally {
            System.out.println();
            System.out.flush();
        }
    }

}
