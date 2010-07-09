package temperance;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;

import temperance.core.Configure;
import temperance.hash.Digest;
import temperance.hash.HashFunction;
import temperance.hashing.MecabHashing;
import temperance.hashing.MecabNodeFilter;
import temperance.server.MsgPackServer;
import temperance.server.Server;
import temperance.server.ProtobufServer;

public class StartStop {
    
    protected void start(Options options, String...args){
        start(options, new GnuParser(), args);
    }
    
    protected void start(Options options, Parser parser, String...args){
        try {
            CommandLine cli = parser.parse(options, args, true);
            
            String memcached = cli.getOptionValue("memc");
            String memcachedPoolSize = cli.getOptionValue("memc_pool", "500");
            String mecabrc = cli.getOptionValue("mecabrc", "/etc/mecabrc");
            
            MecabNodeFilter nodeFilter = MecabHashing.Filter.Nouns;
            if(cli.hasOption("mecab_node_filter_nouns")){
                nodeFilter = MecabHashing.Filter.Default;
            }
            HashFunction fullTextHashFunction = Digest.MD5;
            if(cli.hasOption("ft_hash_sha1")){
                fullTextHashFunction = Digest.SHA1;
            }
            ServerFactory factory = ServerFactory.Protobuf;
            if(cli.hasOption("rpc_msgpack")){
                factory = ServerFactory.Msgpack;
            }
            
            String iniThreads = cli.getOptionValue("iniThreads", "100");
            String maxThreads = cli.getOptionValue("maxThreads", "500");
            
            String port = cli.getOptionValue("p", "17001");
            boolean daemonize = cli.hasOption("daemonize");
            
            Configure configure = new Configure();
            configure.setMemcached(memcached);
            configure.setMaxConnectionPoolSize(Integer.parseInt(memcachedPoolSize));
            configure.setMecabrc(mecabrc);
            configure.setFullTextHashFunction(fullTextHashFunction);
            configure.setNodeFilter(nodeFilter);
            configure.setInitialThreadPoolSize(Integer.parseInt(iniThreads));
            configure.setMaxThreadPoolSize(Integer.parseInt(maxThreads));
            
            Server server = factory.createServer(configure, daemonize, Integer.parseInt(port));
            server.start();
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(StartStop.class.getSimpleName(), options);
            System.exit(0);
        }
    }
    
    protected void stop(String...args){
        Options options = new Options();
        Option port = new Option("p", "port", true, "server port");
        port.setRequired(true);
        
        options.addOptionGroup(rpcServer());
        options.addOption(port);
        stop(options, new GnuParser(), args);
    }
    
    protected void stop(Options options, Parser parser, String...args){
        try {
            CommandLine cli = parser.parse(options, args, true);

            ServerFactory factory = ServerFactory.Protobuf;
            if(cli.hasOption("rpc_msgpack")){
                factory = ServerFactory.Msgpack;
            }
            String port = cli.getOptionValue("p", "17001");
        
            Configure nullobj = new Configure();
            Server server = factory.createServer(nullobj, false, Integer.parseInt(port));
            server.shutdown();
        } catch(ParseException e){
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(StartStop.class.getSimpleName(), options);
            System.exit(0);
        }
    }
    
    protected static Options createCliOptions(){
        Option memcached = new Option("memc", "memcached", true, "memcached server string(ex. host01:11211,host02:11211)");
        memcached.setRequired(true);
        
        Option memcachedPoolSize = new Option("memc_pool", "memcached_pool", true, "memcached connection poolsize(default: 500)");
        memcachedPoolSize.setRequired(false);
        
        Option mecabrc = new Option("mecabrc", "mecabrc", true, "mecabrc path(default /etc/mecabrc)");
        mecabrc.setRequired(false);
        
        OptionGroup mecabNodeFilter = new OptionGroup();
        mecabNodeFilter.addOption(new Option("mecab_node_filter_none", false, "none filter"));
        mecabNodeFilter.addOption(new Option("mecab_node_filter_nouns", false, "nouns node filter(default)"));
        mecabNodeFilter.setRequired(false);
        
        OptionGroup hashFunction = new OptionGroup();
        hashFunction.addOption(new Option("ft_hash_md5", false, "fulltext hash function MD5(default)"));
        hashFunction.addOption(new Option("ft_hash_sha1", false, "fulltext hash function SHA1"));
        hashFunction.setRequired(false);

        OptionGroup rpcServer = rpcServer();
        
        Option iniThreads = new Option("iniThreads", true, "initial thread pool size(default 100)");
        iniThreads.setRequired(false);
        Option maxThreads = new Option("maxThreads", true, "max thread pool size(default 500)");
        maxThreads.setRequired(false);

        Option port = new Option("p", "port", true, "server port");
        port.setRequired(false);
        
        Option daemonize = new Option("daemonize", false, "daemonize process");
        daemonize.setRequired(false);
        
        Options options = new Options();
        options.addOption(memcached);
        options.addOption(memcachedPoolSize);
        options.addOption(mecabrc);
        options.addOptionGroup(mecabNodeFilter);
        options.addOptionGroup(hashFunction);
        options.addOptionGroup(rpcServer);
        options.addOption(iniThreads);
        options.addOption(maxThreads);
        options.addOption(port);
        options.addOption(daemonize);
        return options;
    }
    
    protected static OptionGroup rpcServer(){
        OptionGroup rpcServer = new OptionGroup();
        rpcServer.addOption(new Option("rpc_protobuf", false, "protouf rpc(default)"));
        rpcServer.addOption(new Option("rpc_msgpack", false, "msgpack rpc"));
        rpcServer.setRequired(false);
        return rpcServer;
    }
    
    protected static enum ServerFactory {
        Protobuf {
            public Server createServer(Configure ctx, boolean daemonize, int port){
                return new ProtobufServer(ctx, daemonize, port);
            }
        },
        Msgpack {
            public Server createServer(Configure ctx, boolean daemonize, int port){
                return new MsgPackServer(ctx, daemonize, port);
            }
        },
        ;
        public abstract Server createServer(Configure ctx, boolean daemonize, int port);
    }
}
