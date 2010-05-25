package temperance;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import temperance.handler.Context;
import temperance.server.FullTextServer;
import temperance.server.ListServer;
import temperance.server.MapServer;
import temperance.server.Server;

public class Start {
    
    public static void main(String...args){
        Options opts = createCliOptions();
        GnuParser parser = new GnuParser();
        try {
            CommandLine cli = parser.parse(opts, args, true);
            
            String host = cli.getOptionValue("h", "localhost");
            String port = cli.getOptionValue("p", "11211");
            boolean daemonize = cli.hasOption("daemonize");
            
            Context ctx = new Context();
            ctx.setHost(host);
            ctx.setPort(Integer.valueOf(port).intValue());
            
            start(new FullTextServer(ctx, daemonize, 17001));
            start(new ListServer(ctx, daemonize, 17002));
            start(new MapServer(ctx, daemonize, 17003));
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(Start.class.getSimpleName(), opts);
            System.exit(0);
        }
    }
    
    protected static void start(Server s){
        s.start();
    }
    
    protected static Options createCliOptions(){
        Option host = new Option("h", "host", true, "memcached server hostname");
        host.setRequired(true);

        Option port = new Option("p", "port", true, "memcached server port");
        port.setRequired(true);
        
        Option daemonize = new Option("daemonize", false, "daemonize process");
        
        Options options = new Options();
        options.addOption(host);
        options.addOption(port);
        options.addOption(daemonize);
        return options;
    }
}
