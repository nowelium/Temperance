package temperance.handler.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import libmemcached.exception.LibMemcachedException;

import org.chasen.mecab.wrapper.Tagger;

import temperance.hash.HashFunction;
import temperance.ql.InternalFunction;
import temperance.storage.MemcachedFullTextList;
import temperance.util.FullTextUtil;

public class MecabFunction implements InternalFunction {
    
    protected static final int SPLIT = 3000;
    
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
            List<Long> hashes = FullTextUtil.mecab(hashFunction, tagger, str);
            for(Long hash: hashes){
                String hashString = Long.toString(hash);
                long count = list.count(hashString);
                for(long i = 0; i < count; i += SPLIT){
                    returnValue.addAll(list.get(hashString, i, SPLIT));
                }
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
