package stroom.query.api.v2;

import org.junit.jupiter.api.Test;
import stroom.docref.DocRef;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SearchRequestBuilderTest {
    @Test
    public void doesBuild() {
        // Given
        final String dateTimeLocale = "en-gb";
        final boolean incremental = true;
        final String queryKeyUUID = UUID.randomUUID().toString();
        final String resultRequestComponentId0 = "someResultComponentId0";
        final String resultRequestComponentId1 = "someResultComponentId1";
        final String dataSourceUuid = UUID.randomUUID().toString();

        // When
        final SearchRequest searchRequest = new SearchRequest.Builder()
                .query(new Query.Builder()
                    .dataSource(new DocRef.Builder()
                        .uuid(dataSourceUuid)
                        .build())
                    .build())
                .dateTimeLocale(dateTimeLocale)
                .incremental(incremental)
                .key(queryKeyUUID)
                .addResultRequests(new ResultRequest.Builder()
                    .componentId(resultRequestComponentId0)
                    .build())
                .addResultRequests(new ResultRequest.Builder()
                    .componentId(resultRequestComponentId1)
                    .build())
                .build();

        // Then
        assertEquals(dateTimeLocale, searchRequest.getDateTimeLocale());
        assertEquals(incremental, searchRequest.getIncremental());
        assertNotNull(searchRequest.getKey());
        assertEquals(queryKeyUUID, searchRequest.getKey().getUuid());
        assertEquals(2, searchRequest.getResultRequests().size());
        assertEquals(resultRequestComponentId0, searchRequest.getResultRequests().get(0).getComponentId());
        assertEquals(resultRequestComponentId1, searchRequest.getResultRequests().get(1).getComponentId());
        assertNotNull(searchRequest.getQuery());
        assertNotNull(searchRequest.getQuery().getDataSource());
        assertEquals(dataSourceUuid, searchRequest.getQuery().getDataSource().getUuid());
    }
}
