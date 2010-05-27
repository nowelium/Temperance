package temperance.handler.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import libmemcached.exception.LibMemcachedException;

import org.chasen.mecab.wrapper.MecabNode;
import org.chasen.mecab.wrapper.Node;
import org.chasen.mecab.wrapper.Path;
import org.chasen.mecab.wrapper.Tagger;

import temperance.hash.HashFunction;
import temperance.ql.InternalFunction;
import temperance.storage.MemcachedFullTextList;

public class MecabFunction implements InternalFunction {
    
    protected final FunctionContext context;
    
    public MecabFunction(FunctionContext context){
        this.context = context;
    }

    public List<String> in(String key, List<String> args) {
        HashFunction hashFunction = context.getHashFunction();
        Tagger tagger = context.getTagger();
        String str = args.get(0);
        
        try {
            MemcachedFullTextList list = new MemcachedFullTextList(context.getClient(), key);
            List<String> returnValue = new ArrayList<String>();
            for(MecabNode<Node, Path> node: tagger.iterator(str)){
                long hash = hashFunction.hash(node.getSurface());
                List<String> values = list.get(Long.toString(hash), 0, 3000);
                returnValue.addAll(values);
            }
            return returnValue;
        } catch(LibMemcachedException e){
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<String> not(String key, List<String> args) {
        return Collections.emptyList();
    }

}
