package temperance;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;

import temperance.ft.MecabHashing;
import temperance.ft.MecabNodeFilter;
import temperance.handler.Context;
import temperance.hash.Hash;
import temperance.hash.HashFunction;
import temperance.server.Server;
import temperance.server.TemperanceServer;

public class StartStop {
    
    protected void start(Options options, String...args){
        start(options, new GnuParser(), args);
    }
    
    protected void start(Options options, Parser parser, String...args){
        try {
            CommandLine cli = parser.parse(options, args, true);
            
            String memcached = cli.getOptionValue("memc");
            String memcachedPoolSize = cli.getOptionValue("memc_pool", "300");
            String mecabrc = cli.getOptionValue("mecabrc", "/opt/local/etc/mecabrc");
            
            MecabNodeFilter nodeFilter = MecabHashing.Filter.Nouns;
            if(cli.hasOption("mecab_node_filter_nouns")){
                nodeFilter = MecabHashing.Filter.Default;
            }
            HashFunction fullTextHashFunction = Hash.MD5;
            if(cli.hasOption("ft_hash_sha1")){
                fullTextHashFunction = Hash.SHA1;
            }
            
            String port = cli.getOptionValue("p", "17001");
            boolean daemonize = cli.hasOption("daemonize");
            
            Context ctx = new Context();
            ctx.setMemcached(memcached);
            ctx.setMemcachedPoolSize(Integer.parseInt(memcachedPoolSize));
            ctx.setMecabrc(mecabrc);
            ctx.setFullTextHashFunction(fullTextHashFunction);
            ctx.setNodeFilter(nodeFilter);
            
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
    
    protected Server createServer(Context ctx, boolean daemonize, int port) {
        return new TemperanceServer(ctx, daemonize, port);
    }
    
    protected static Options createCliOptions(){
        Option memcached = new Option("memc", "memcached", true, "memcached server string(ex. host01:11211,host02:11211)");
        memcached.setRequired(true);
        
        Option memcachedPoolSize = new Option("memc_pool", "memcached_pool", true, "memcached connection poolsize");
        memcachedPoolSize.setRequired(false);
        
        Option mecabrc = new Option("mecabrc", "mecabrc", true, "mecabrc path(ex. /etc/mecabrc)");
        mecabrc.setRequired(false);
        
        OptionGroup mecabNodeFilter = new OptionGroup();
        mecabNodeFilter.addOption(new Option("mecab_node_filter_none", false, "none filter"));
        mecabNodeFilter.addOption(new Option("mecab_node_filter_nouns", false, "nouns node filter(default)"));
        
        OptionGroup hashFunction = new OptionGroup();
        hashFunction.addOption(new Option("ft_hash_md5", false, "fulltext hash function MD5"));
        hashFunction.addOption(new Option("ft_hash_sha1", false, "fulltext hash function SHA1"));
        hashFunction.setRequired(false);

        Option port = new Option("p", "port", true, "server port");
        port.setRequired(false);
        
        Option daemonize = new Option("daemonize", false, "daemonize process");
        daemonize.setRequired(false);
        
        Options options = new Options();
        options.addOption(memcached);
        options.addOption(memcachedPoolSize);
        options.addOption(mecabrc);
        options.addOptionGroup(hashFunction);
        options.addOption(port);
        options.addOption(daemonize);
        return options;
    }
}
