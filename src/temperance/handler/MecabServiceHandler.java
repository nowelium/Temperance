package temperance.handler;

import org.chasen.mecab.wrapper.MecabNode;
import org.chasen.mecab.wrapper.Node;
import org.chasen.mecab.wrapper.Path;
import org.chasen.mecab.wrapper.Tagger;

import temperance.ft.MecabHashing;
import temperance.ft.MecabNodeFilter;
import temperance.hash.HashFunction;
import temperance.protobuf.Mecab.MecabService;
import temperance.protobuf.Mecab.Request;
import temperance.protobuf.Mecab.Response;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

public class MecabServiceHandler implements MecabService.BlockingInterface {

    protected final Context context;
    
    protected final HashFunction hashFunction;
    
    protected final MecabNodeFilter nodeFilter;

    protected final Tagger tagger;
    
    public MecabServiceHandler(Context context){
        this.context = context;
        this.hashFunction = context.getFullTextHashFunction();
        this.nodeFilter = context.getNodeFilter();
        this.tagger = Tagger.create("-r", context.getMecabrc());
    }
    
    public Response.Parse parse(RpcController controller, Request.Parse request) throws ServiceException {
        final String str = request.getStr();
        MecabHashing mecab = new MecabHashing(hashFunction, tagger, nodeFilter);
        
        Response.Parse.Builder builder = Response.Parse.newBuilder();
        for(MecabNode<Node, Path> node: mecab.parseToNode(str)){
            Response.Node.Builder nodeBuilder = Response.Node.newBuilder();
            nodeBuilder.setSurface(node.getSurface());
            nodeBuilder.setFeature(node.getFeature());
            builder.addNodes(nodeBuilder.build());
        }
        return builder.build();
    }

}
