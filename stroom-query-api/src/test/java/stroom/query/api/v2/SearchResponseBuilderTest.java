package stroom.query.api.v2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        final SearchResponse searchResponse = new SearchResponse.FlatResultBuilder()
                .complete(true)
                .addErrors(error0, error1)
                .addHighlights(highlight0)
                .addResults(new FlatResult.Builder()
                        .componentId(flatResultComponentId0)
                        .build())
                .addResults(new FlatResult.Builder()
                        .componentId(flatResultComponentId1)
                        .build())
                .addResults(new FlatResult.Builder()
                    .componentId(flatResultComponentId2)
                    .build())
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
        final SearchResponse searchResponse = new SearchResponse.TableResultBuilder()
                .complete(true)
                .addErrors(error0, error1)
                .addHighlights(highlight0)
                .addResults(new TableResult.Builder()
                        .componentId(flatResultComponentId0)
                        .build())
                .addResults(new TableResult.Builder()
                        .componentId(flatResultComponentId1)
                        .build())
                .addResults(new TableResult.Builder()
                        .componentId(flatResultComponentId2)
                        .build())
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
