package stroom.query.v2;

import org.junit.Assert;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;

public class TestMaxResults {

    @Test
    public void testSize_bothNull() {
        doTest( null, null, 0, Integer.MAX_VALUE);
        doTest( null, null, 1, Integer.MAX_VALUE);
    }

    @Test
    public void testSize_userIsNull() {
        List<Integer> defaultSizes = Arrays.asList(100,10,1);
        doTest( null, defaultSizes, 0, defaultSizes.get(0));
        doTest( null, defaultSizes, 1,defaultSizes.get(1));
        doTest( null, defaultSizes, 2, defaultSizes.get(2));
        doTest( null, defaultSizes, 3, defaultSizes.get(2));
    }

    @Test
    public void testSize_defaultIsNull() {
        List<Integer> userSizes = Arrays.asList(100,10,1);
        doTest( userSizes, null, 0, userSizes.get(0));
        doTest( userSizes, null, 1, userSizes.get(1));
        doTest( userSizes, null, 2, userSizes.get(2));
        doTest( userSizes, null, 3, userSizes.get(2));
    }

    @Test
    public void testSize_bothSuppliedSameSize() {
        List<Integer> userSizes = Arrays.asList(100,10,1);
        List<Integer> defaultSizes = Arrays.asList(2000,200,20);

        //user sizes are all smaller so will use those
        doTest( userSizes, defaultSizes, 0, userSizes.get(0));
        doTest( userSizes, defaultSizes, 1, userSizes.get(1));
        doTest( userSizes, defaultSizes, 2, userSizes.get(2));
        doTest( userSizes, defaultSizes, 3, userSizes.get(2));
    }

    @Test
    public void testSize_bothSuppliedListSizeMismatch() {
        List<Integer> userSizes = Arrays.asList(100);
        List<Integer> defaultSizes = Arrays.asList(2000,200,20);

        //user sizes are all smaller so will use those
        doTest( userSizes, defaultSizes, 0, userSizes.get(0));
        doTest( userSizes, defaultSizes, 1, defaultSizes.get(1));
        doTest( userSizes, defaultSizes, 2, defaultSizes.get(2));
        doTest( userSizes, defaultSizes, 3, defaultSizes.get(2));
    }

    private void doTest(List<Integer> userSizes, List<Integer> defaultSizes, int depth, int expectedSize) {
        MaxResults maxResults = new MaxResults(userSizes, defaultSizes);
        Assert.assertEquals(expectedSize, maxResults.size(depth));
    }

}