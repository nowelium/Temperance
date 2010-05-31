package temperance.ft;

import org.chasen.mecab.wrapper.MecabNode;
import org.chasen.mecab.wrapper.Node;
import org.chasen.mecab.wrapper.Path;

public interface MecabNodeFilter {
    
    public boolean accept(MecabNode<Node, Path> node);

    public static enum CharType {
        Alfabet((byte) 9),
        Particle((byte) 6),
        Numeric((byte) 3),
        Symbol((byte) 0)
        ;
        
        private final byte type;
        private CharType(byte type){
            this.type = type;
        }
        public byte getType(){
            return type;
        }
        public boolean equals(byte type){
            return this.type == type;
        }
    }

}
