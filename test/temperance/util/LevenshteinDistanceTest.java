package temperance.util;

import org.junit.Assert;
import org.junit.Test;

public class LevenshteinDistanceTest {

    @Test
    public void distance() {
        Assert.assertEquals(LevenshteinDistance.distance("apple", "apple"), 0);
        Assert.assertEquals(LevenshteinDistance.distance("foobar", "foober"), 1);
        Assert.assertEquals(LevenshteinDistance.distance("apple", "april"), 3);
        Assert.assertEquals(LevenshteinDistance.distance("aaa", "bbbb"), 4);
        Assert.assertEquals(LevenshteinDistance.distance("aaaa", "ccc"), 4);
        Assert.assertEquals(LevenshteinDistance.distance("aaa", "bbb"), 3);
        Assert.assertEquals(LevenshteinDistance.distance("hoge", "hoeg"), 2);
        Assert.assertEquals(LevenshteinDistance.distance("1234567890", "1234512345"), 5);
    }
    
    @Test
    public void distance_multibyte() {
        Assert.assertEquals(LevenshteinDistance.distance("あいうえお", "あいうえお"), 0);
        Assert.assertEquals(LevenshteinDistance.distance("あいうえお", "あいうおう"), 2);
        Assert.assertEquals(LevenshteinDistance.distance("あいうえおか", "あいうえおかき"), 1);
    }

}
