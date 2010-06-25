package temperance.hashing;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import temperance.hash.Digest;
import temperance.hash.Hash;

public class SSVHashingTest {

    @Test
    public void parse() {
        SSVHashing ssv = new SSVHashing(Digest.MD5);
        List<Hash> hashes = ssv.parse("    a   b     c    ");
        Assert.assertEquals(hashes.size(), 3);
    }
    
    @Test
    public void parse_multibyte() {
        SSVHashing ssv = new SSVHashing(Digest.MD5);
        List<Hash> hashes = ssv.parse("    a  　　  　　b　　   　  c    ");
        Assert.assertEquals(hashes.size(), 3);
    }

    @Test
    public void split() {
        SSVHashing ssv = new SSVHashing(Digest.MD5);
        String[] sp = ssv.split("a b c");
        Assert.assertEquals(sp.length, 3);
        Assert.assertEquals(sp[0], "a");
        Assert.assertEquals(sp[1], "b");
        Assert.assertEquals(sp[2], "c");
    }

    @Test
    public void split_multibyte() {
        SSVHashing ssv = new SSVHashing(Digest.MD5);
        String[] sp = ssv.split("あ　い　う");
        Assert.assertEquals(sp.length, 3);
        Assert.assertEquals(sp[0], "あ");
        Assert.assertEquals(sp[1], "い");
        Assert.assertEquals(sp[2], "う");
    }
}
