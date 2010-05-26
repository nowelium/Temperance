package temperance;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;

import temperance.handler.Context;

public abstract class StartStop {
    
    protected void start(Options options, String...args){
        start(options, new GnuParser(), args);
    }
    
    protected void start(Options options, Parser parser, String...args){
        try {
            CommandLine cli = parser.parse(options, args, true);
            
            String host = cli.getOptionValue("h", "localhost");
            String port = cli.getOptionValue("p", "11211");
            boolean daemonize = cli.hasOption("daemonize");
            
            Context ctx = new Context();
            ctx.setHost(host);
            ctx.setPort(Integer.valueOf(port).intValue());
            
            start(ctx, daemonize);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(StartStop.class.getSimpleName(), options);
            System.exit(0);
        }
    }
    
    protected void stop(String...args){
        shutdown();
    }
    
    protected abstract void start(Context context, boolean daemonize);

    protected abstract void shutdown();
    
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
