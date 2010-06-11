package temperance.rpc.impl;

import org.chasen.mecab.wrapper.MecabNode;
import org.chasen.mecab.wrapper.Node;
import org.chasen.mecab.wrapper.Path;
import org.chasen.mecab.wrapper.Tagger;

import temperance.core.Configure;
import temperance.exception.RpcException;
import temperance.ft.MecabHashing;
import temperance.ft.MecabNodeFilter;
import temperance.hash.HashFunction;
import temperance.rpc.RpcMecab;

public class RpcMecabImpl implements RpcMecab {

    protected final Configure configure;
    
    protected final HashFunction hashFunction;
    
    protected final MecabNodeFilter nodeFilter;

    protected final Tagger tagger;
    
    public RpcMecabImpl(Configure configure){
        this.configure = configure;
        this.hashFunction = configure.getFullTextHashFunction();
        this.nodeFilter = configure.getNodeFilter();
        this.tagger = Tagger.create("-r", configure.getMecabrc());
    }
    
    public Response.Parse parse(Request.Parse request) throws RpcException {
        final String str = request.str;
        MecabHashing mecab = new MecabHashing(hashFunction, tagger, nodeFilter);
        
        Response.Parse response = Response.Parse.newInstance();
        for(MecabNode<Node, Path> node: mecab.parseToNode(str)){
            Response.Node responseNode = Response.Node.newInstance();
            responseNode.surface = node.getSurface();
            responseNode.feature = node.getFeature();
            response.nodes.add(responseNode);
        }
        return response;
    }

}
