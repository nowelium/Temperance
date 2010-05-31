package temperance.ft;

import java.util.ArrayList;
import java.util.List;

import org.chasen.mecab.wrapper.MecabNode;
import org.chasen.mecab.wrapper.Node;
import org.chasen.mecab.wrapper.Path;
import org.chasen.mecab.wrapper.Tagger;

import temperance.hash.HashFunction;

public class Mecab implements Hashing {
    
    protected final HashFunction function;
    
    protected final Tagger tagger;
    
    protected final MecabNodeFilter filter;
    
    public Mecab(HashFunction function, Tagger tagger){
        this(function, tagger, Filter.Default);
    }
    
    public Mecab(HashFunction function, Tagger tagger, MecabNodeFilter filter){
        this.function = function;
        this.tagger = tagger;
        this.filter = filter;
    }
    
    public List<Long> parse(String str) {
        List<Long> hashes = new ArrayList<Long>();
        for(MecabNode<Node, Path> node: tagger.iterator(str)){
            if(!filter.accept(node)){
                continue;
            }
            long hash = function.hash(node.getSurface());
            hashes.add(Long.valueOf(hash));
        }
        return hashes;
    }

    public List<Long> parse(String str, int split) {
        Gram gram = new Gram(function, split);
        List<Long> hashes = new ArrayList<Long>();
        for(MecabNode<Node, Path> node: tagger.iterator(str)){
            if(!filter.accept(node)){
                continue;
            }
            String surface = node.getSurface();
            hashes.addAll(gram.parse(surface));
        }
        return hashes;
    }
    
    public static enum Filter implements MecabNodeFilter {
        Default() {
            public boolean accept(MecabNode<Node, Path> node) {
                if(null == node){
                    return false;
                }
                return true;
            }
        },
        Nouns(CharType.Alfabet, CharType.Particle, CharType.Numeric, CharType.Symbol) {
            public boolean accept(MecabNode<Node, Path> node){
                if(!Default.accept(node)){
                    return false;
                }
                if(ignoreValues.contains(Byte.valueOf(node.getCharType()))){
                    return false;
                }
                return true;
            }
        },
        ;
        
        protected final List<Byte> ignoreValues = new ArrayList<Byte>();
        
        private Filter(CharType...ignore){
            for(CharType ct: ignore){
                ignoreValues.add(Byte.valueOf(ct.getType()));
            }
        }
    }
    
}
