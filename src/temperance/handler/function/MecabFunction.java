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
    
    public List<String> deleteIn(String key, List<String> args) {
        return null;
    }

    public List<String> deleteNot(String key, List<String> args) {
        return null;
    }

    public List<String> selectIn(String key, List<String> args) {
        HashFunction hashFunction = context.getHashFunction();
        Tagger tagger = context.getTagger();
        String str = args.get(0);
        
        try {
            MemcachedFullTextList list = new MemcachedFullTextList(context.getClient());
            List<String> returnValue = new ArrayList<String>();
            List<Long> hashes = FullTextUtil.mecab(hashFunction, tagger, str);
            for(Long hash: hashes){
                long count = list.count(key, hash);
                for(long i = 0; i < count; i += SPLIT){
                    returnValue.addAll(list.get(key, hash, i, SPLIT));
                }
            }
            return returnValue;
        } catch(LibMemcachedException e){
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<String> selectNot(String key, List<String> args) {
        HashFunction hashFunction = context.getHashFunction();
        Tagger tagger = context.getTagger();
        String str = args.get(0);
        
        try {
            MemcachedFullTextList list = new MemcachedFullTextList(context.getClient());
            List<String> returnValue = new ArrayList<String>();
            List<Long> ignoreHashes = FullTextUtil.mecab(hashFunction, tagger, str);
            // FIXME: search not in key
            for(Long ignoreHash: ignoreHashes){
                long count = list.count(key);
                for(long i = 0; i < count; i += SPLIT){
                    List<String> storedHashes = list.get(key, i, SPLIT);
                    storedHashes.remove(ignoreHash.toString());
                    for(String hash: storedHashes){
                        returnValue.addAll(list.get(key, Long.valueOf(hash), i, SPLIT));
                    }
                }
            }
            return returnValue;
        } catch(LibMemcachedException e){
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

}
