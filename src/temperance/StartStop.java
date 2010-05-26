package temperance;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;

import temperance.handler.Context;
import temperance.server.Server;

public abstract class StartStop {
    
    protected void start(Options options, String...args){
        start(options, new GnuParser(), args);
    }
    
    protected void start(Options options, Parser parser, String...args){
        try {
            CommandLine cli = parser.parse(options, args, true);
            
            String memcached = cli.getOptionValue("m");
            String port = cli.getOptionValue("p");
            boolean daemonize = cli.hasOption("daemonize");
            
            Context ctx = new Context();
            ctx.setMemcached(memcached);
            
            Server server = createServer(ctx, daemonize, Integer.parseInt(port));
            server.start();
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(StartStop.class.getSimpleName(), options);
            System.exit(0);
        }
    }
    
    protected void stop(){
        Context nullobj = new Context();
        Server server = createServer(nullobj, false, 0);
        server.shutdown();
    }
    
    protected abstract Server createServer(Context ctx, boolean daemonize, int port);
    
    protected static Options createCliOptions(){
        Option host = new Option("m", "memcached", true, "memcached server string(ex. host01:11211,host02:11211)");
        host.setRequired(true);

        Option port = new Option("p", "port", true, "server port");
        port.setRequired(true);
        
        Option daemonize = new Option("daemonize", false, "daemonize process");
        
        Options options = new Options();
        options.addOption(host);
        options.addOption(port);
        options.addOption(daemonize);
        return options;
    }
}
