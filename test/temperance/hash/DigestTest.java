package temperance.hash;

import org.junit.Test;

public class DigestTest {

    @Test
    public void testHash() {
        System.out.println(Digest.MD5.hash("hello world").hashValue());
        System.out.println(Digest.SHA1.hash("hello world").hashValue());
    }

}
