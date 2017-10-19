package stroom.query.api.v2;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FilterBuilderTest {
    @Test
    public void doesBuild() {
        final String excludes = "stuff to exclude **";
        final String includes = "stuff to include &&";

        final Filter filter = new Filter.Builder<>()
                .excludes(excludes)
                .includes(includes)
                .build();

        assertEquals(excludes, filter.getExcludes());
        assertEquals(includes, filter.getIncludes());
    }
}
