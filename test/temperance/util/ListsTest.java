package temperance.util;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ListsTest {

    @Test
    public void testUnique() {
        // Lists.unique unsafe order
        {
            List<String> a = Arrays.asList("a", "b", "c");
            Assert.assertEquals(Lists.unique(a), Lists.unique(Arrays.asList("a", "b", "c")));
        }
        {
            List<String> a = Arrays.asList("a", "a", "b");
            Assert.assertEquals(Lists.unique(a), Lists.unique(Arrays.asList("a", "b")));
        }
        {
            List<String> a = Arrays.asList("a", "a", "b", "b");
            Assert.assertEquals(Lists.unique(a), Lists.unique(Arrays.asList("a", "b")));
        }
    }

    @Test
    public void testIntersect() {
        {
            List<String> a = Arrays.asList("a", "b", "c");
            List<String> b = Arrays.asList("b", "c");
            Assert.assertEquals(Lists.intersect(a, b), Arrays.asList("b", "c"));
        }
        {
            List<String> a = Arrays.asList("a", "b", "c");
            List<String> b = Arrays.asList();
            // no intersect
            Assert.assertEquals(Lists.intersect(a, b), Arrays.asList());
        }
        {
            List<String> a = Arrays.asList("a", "b", "c");
            List<String> b = Arrays.asList("a", "a", "a", "a");
            Assert.assertEquals(Lists.intersect(a, b), Arrays.asList("a"));
        }
        {
            List<String> a = Arrays.asList("a", "b", "c");
            List<String> b = Arrays.asList("a", "a", "a", "a", "c");
            Assert.assertEquals(Lists.intersect(a, b), Arrays.asList("a", "c"));
        }
        {
            List<String> a = Arrays.asList("a", "b", "c");
            List<String> b = Arrays.asList("b", "a");
            // left args list order
            Assert.assertEquals(Lists.intersect(a, b), Arrays.asList("a", "b"));
        }
    }

    @Test
    public void testSubtract() {
        {
            List<String> a = Arrays.asList("a", "b", "c");
            List<String> b = Arrays.asList("b", "c");
            Assert.assertEquals(Lists.subtract(a, b), Arrays.asList("a"));
        }
        {
            List<String> a = Arrays.asList("a", "b", "c");
            List<String> b = Arrays.asList("b");
            Assert.assertEquals(Lists.subtract(a, b), Arrays.asList("a", "c"));
        }
        {
            List<String> a = Arrays.asList("a", "b", "c");
            List<String> b = Arrays.asList();
            Assert.assertEquals(Lists.subtract(a, b), Arrays.asList("a", "b", "c"));
        }
        {
            List<String> a = Arrays.asList("a", "b", "c");
            List<String> b = Arrays.asList("1", "2", "3");
            Assert.assertEquals(Lists.subtract(a, b), Arrays.asList("a", "b", "c"));
        }
    }

}
