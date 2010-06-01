package temperance.ft;

import java.util.List;

import org.chasen.mecab.wrapper.MecabNode;
import org.chasen.mecab.wrapper.Node;
import org.chasen.mecab.wrapper.Path;
import org.chasen.mecab.wrapper.Tagger;

import temperance.hash.HashFunction;
import temperance.util.Lists;

public class MecabHashing implements Hashing {
    
    protected final HashFunction function;
    
    protected final Tagger tagger;
    
    protected final MecabNodeFilter filter;
    
    public MecabHashing(HashFunction function, Tagger tagger){
        this(function, tagger, Filter.Default);
    }
    
    public MecabHashing(HashFunction function, Tagger tagger, MecabNodeFilter filter){
        this.function = function;
        this.tagger = tagger;
        this.filter = filter;
    }
    
    public List<MecabNode<Node, Path>> parseToNode(String str){
        List<MecabNode<Node, Path>> nodes = Lists.newArrayList();
        for(MecabNode<Node, Path> node: tagger.iterator(str)){
            if(!filter.accept(node)){
                continue;
            }
            nodes.add(node);
        }
        return nodes;
    }
    
    public List<String> parseToString(String str){
        List<String> surfaces = Lists.newArrayList();
        for(MecabNode<Node, Path> node: parseToNode(str)){
            surfaces.add(node.getSurface());
        }
        return surfaces;
    }
    
    public List<Long> parse(String str) {
        List<Long> hashes = Lists.newArrayList();
        for(String surface: parseToString(str)){
            long hash = function.hash(surface);
            hashes.add(Long.valueOf(hash));
        }
        return hashes;
    }

    public List<Long> parse(String str, int split) {
        GramHashing gram = new GramHashing(function, split);
        List<Long> hashes = Lists.newArrayList();
        for(String surface: parseToString(str)){
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
        
        protected final List<Byte> ignoreValues = Lists.newArrayList();
        
        private Filter(CharType...ignore){
            for(CharType ct: ignore){
                ignoreValues.add(Byte.valueOf(ct.getType()));
            }
        }
    }
    
}
