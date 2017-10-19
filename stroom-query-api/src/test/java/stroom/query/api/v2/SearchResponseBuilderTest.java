package stroom.query.api.v2;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SearchResponseBuilderTest {
    @Test
    public void doesBuildFlat() {
        // Given
        final String error0 = "something went wrong 0";
        final String error1 = "something went wrong 1";
        final String highlight0 = "SOMETHING";
        final String flatResultComponentId0 = "flatResult0";
        final String flatResultComponentId1 = "flatResult1";
        final String flatResultComponentId2 = "flatResult2";

        // When
        final SearchResponse searchResponse = new SearchResponse.Builder()
                .complete(true)
                .addErrors(error0, error1)
                .addHighlights(highlight0)
                .addFlatResult()
                    .componentId(flatResultComponentId0)
                    .end()
                .addFlatResult()
                    .componentId(flatResultComponentId1)
                    .end()
                .addFlatResult()
                    .componentId(flatResultComponentId2)
                    .end()
                .build();

        // Then
        assertEquals(2, searchResponse.getErrors().size());
        assertTrue(searchResponse.getComplete());
        assertTrue(searchResponse.getErrors().contains(error0));
        assertTrue(searchResponse.getErrors().contains(error1));
        assertEquals(1, searchResponse.getHighlights().size());
        assertEquals(highlight0, searchResponse.getHighlights().get(0));
        long resultCount = searchResponse.getResults().stream()
                .filter(result -> result instanceof FlatResult)
                .map(result -> (FlatResult) result)
                .map(Result::getComponentId)
                .filter(s -> s.startsWith("flatResult"))
                .count();
        assertEquals(3L, resultCount);
    }

    @Test
    public void doesBuildTable() {
        // Given
        final String error0 = "something went wrong 0";
        final String error1 = "something went wrong 1";
        final String highlight0 = "SOMETHING";
        final String flatResultComponentId0 = "tableResult0";
        final String flatResultComponentId1 = "tableResult1";
        final String flatResultComponentId2 = "tableResult2";

        // When
        final SearchResponse searchResponse = new SearchResponse.Builder()
                .complete(true)
                .addErrors(error0, error1)
                .addHighlights(highlight0)
                .addTableResult()
                    .componentId(flatResultComponentId0)
                    .end()
                .addTableResult()
                    .componentId(flatResultComponentId1)
                    .end()
                .addTableResult()
                    .componentId(flatResultComponentId2)
                    .end()
                .build();

        // Then
        assertEquals(2, searchResponse.getErrors().size());
        assertTrue(searchResponse.getComplete());
        assertTrue(searchResponse.getErrors().contains(error0));
        assertTrue(searchResponse.getErrors().contains(error1));
        assertEquals(1, searchResponse.getHighlights().size());
        assertEquals(highlight0, searchResponse.getHighlights().get(0));
        long resultCount = searchResponse.getResults().stream()
                .filter(result -> result instanceof TableResult)
                .map(result -> (TableResult) result)
                .map(Result::getComponentId)
                .filter(s -> s.startsWith("tableResult"))
                .count();
        assertEquals(3L, resultCount);
    }
}
