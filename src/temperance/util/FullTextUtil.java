package temperance.util;

import java.util.ArrayList;
import java.util.List;

import org.chasen.mecab.wrapper.MecabNode;
import org.chasen.mecab.wrapper.Node;
import org.chasen.mecab.wrapper.Path;
import org.chasen.mecab.wrapper.Tagger;

import temperance.hash.HashFunction;

public class FullTextUtil {
    
    public static List<Long> mecab(HashFunction function, Tagger tagger, String str) {
        List<Long> hashes = new ArrayList<Long>();
        for(MecabNode<Node, Path> node: tagger.iterator(str)){
            long hash = function.hash(node.getSurface());
            hashes.add(Long.valueOf(hash));
        }
        return hashes;
    }
    
    public static List<Long> mecab(HashFunction function, Tagger tagger, String str, int split){
        List<Long> hashes = new ArrayList<Long>();
        for(MecabNode<Node, Path> node: tagger.iterator(str)){
            String surface = node.getSurface();
            hashes.addAll(gram(function, surface, split));
        }
        return hashes;
    }
    
    public static List<Long> gram(HashFunction function, String str){
        return gram(function, str, 2);
    }
    
    public static List<Long> gram(HashFunction function, String str, int split){
        List<Long> hashes = new ArrayList<Long>();
        
        int length = str.length();
        for(int i = 0; i < length; ++i){
            int limit = i + split;
            if(length < limit){
                limit = i + 1;
            }
            long hash = function.hash(str.substring(i, limit));
            hashes.add(Long.valueOf(hash));
        }
        return hashes;
    }

}
