package stroom.query.v2;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestStoreSize {

    @Test
    public void testSize_null() {
        doTest( null, 0, Integer.MAX_VALUE);
        doTest( null, 1, Integer.MAX_VALUE);
    }

    @Test
    public void testSize_emptyList() {
        List<Integer> storeSizes = Collections.emptyList();
        doTest( storeSizes, 0, Integer.MAX_VALUE);
        doTest( storeSizes, 1, Integer.MAX_VALUE);
    }

    @Test
    public void testSize_populatedList() {
        List<Integer> storeSizes = Arrays.asList(100,10,1);
        doTest( storeSizes, 0, storeSizes.get(0));
        doTest( storeSizes, 1, storeSizes.get(1));
        doTest( storeSizes, 2, storeSizes.get(2));
        doTest( storeSizes, 3, storeSizes.get(2));
    }

    private void doTest(List<Integer> storeSizes, int depth, int expectedSize) {
        StoreSize storeSize = new StoreSize(storeSizes);
        Assert.assertEquals(expectedSize, storeSize.size(depth));
    }

}