package stroom.query.common.v2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class TestSizes {
    @Test
    void testSize_null() {
        test(null, 0, 1);
        test(null, 1, 1);
    }

    @Test
    void testSize_emptyList() {
        List<Integer> storeSizes = Collections.emptyList();
        test(storeSizes, 0, 1);
        test(storeSizes, 1, 1);
    }

    @Test
    void testSize_populatedList() {
        List<Integer> storeSizes = Arrays.asList(100, 10, 1);
        test(storeSizes, 0, storeSizes.get(0));
        test(storeSizes, 1, storeSizes.get(1));
        test(storeSizes, 2, storeSizes.get(2));
        test(storeSizes, 3, storeSizes.get(2));
    }

    private void test(List<Integer> storeSizes, int depth, int expectedSize) {
        Sizes storeSize = Sizes.create(storeSizes);
        Assertions.assertEquals(expectedSize, storeSize.size(depth));
    }

    @Test
    void testSize_bothNull() {
        testMin(null, null, 0, 1);
        testMin(null, null, 1, 1);
    }

    @Test
    void testSize_bothEmpty() {
        testMin(Collections.emptyList(), Collections.emptyList(), 0, 1);
        testMin(Collections.emptyList(), Collections.emptyList(), 1, 1);
    }

    @Test
    void testSize_userIsNull() {
        List<Integer> defaultSizes = Arrays.asList(100, 10, 1);
        testMin(null, defaultSizes, 0, defaultSizes.get(0));
        testMin(null, defaultSizes, 1, defaultSizes.get(1));
        testMin(null, defaultSizes, 2, defaultSizes.get(2));
        testMin(null, defaultSizes, 3, defaultSizes.get(2));
    }

    @Test
    void testSize_defaultIsNull() {
        List<Integer> userSizes = Arrays.asList(100, 10, 1);
        testMin(userSizes, null, 0, userSizes.get(0));
        testMin(userSizes, null, 1, userSizes.get(1));
        testMin(userSizes, null, 2, userSizes.get(2));
        testMin(userSizes, null, 3, userSizes.get(2));
    }

    @Test
    void testSize_bothSuppliedSameSize() {
        List<Integer> userSizes = Arrays.asList(100, 10, 1);
        List<Integer> defaultSizes = Arrays.asList(2000, 200, 20);

        //user sizes are all smaller so will use those
        testMin(userSizes, defaultSizes, 0, userSizes.get(0));
        testMin(userSizes, defaultSizes, 1, userSizes.get(1));
        testMin(userSizes, defaultSizes, 2, userSizes.get(2));
        testMin(userSizes, defaultSizes, 3, userSizes.get(2));
    }

    @Test
    void testSize_bothSuppliedListSizeMismatch() {
        List<Integer> userSizes = Arrays.asList(100);
        List<Integer> defaultSizes = Arrays.asList(2000, 200, 20);

        //user sizes are all smaller so will use those
        testMin(userSizes, defaultSizes, 0, userSizes.get(0));
        testMin(userSizes, defaultSizes, 1, defaultSizes.get(1));
        testMin(userSizes, defaultSizes, 2, defaultSizes.get(2));
        testMin(userSizes, defaultSizes, 3, defaultSizes.get(2));
    }

    private void testMin(List<Integer> userSizes, List<Integer> defaultSizes, int depth, int expectedSize) {
        Sizes maxResults = Sizes.min(Sizes.create(userSizes), Sizes.create(defaultSizes));
        Assertions.assertEquals(expectedSize, maxResults.size(depth));
    }
}