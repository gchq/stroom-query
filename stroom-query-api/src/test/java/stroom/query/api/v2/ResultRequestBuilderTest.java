package stroom.query.api.v2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ResultRequestBuilderTest {
    @Test
    public void doesBuild() {
        // Given
        final String componentId = "someComponent";
        final ResultRequest.Fetch fetch = ResultRequest.Fetch.CHANGES;
        final String openGroup0 = "someOpenGroup0";
        final String openGroup1 = "someOpenGroup1";
        final Long rangeLength = 70L;
        final Long rangeOffset = 1000L;
        final String queryId0 = "someQueryId0";
        final String queryId1 = "someQueryId1";
        final String queryId2 = "someQueryId2";

        // When
        final ResultRequest resultRequest = new ResultRequest.Builder()
                .componentId(componentId)
                .fetch(fetch)
                .addOpenGroups(openGroup0, openGroup1)
                .requestedRange(new OffsetRange.Builder()
                        .length(rangeLength)
                        .offset(rangeOffset)
                        .build())
                .addMappings(new TableSettings.Builder()
                        .queryId(queryId0)
                        .build())
                .addMappings(new TableSettings.Builder()
                        .queryId(queryId1)
                        .build())
                .addMappings(new TableSettings.Builder()
                        .queryId(queryId2)
                        .build())
                .build();

        // Then
        assertEquals(componentId, resultRequest.getComponentId());
        assertEquals(fetch, resultRequest.getFetch());

        assertEquals(2, resultRequest.getOpenGroups().size());
        assertTrue(resultRequest.getOpenGroups().contains(openGroup0));
        assertTrue(resultRequest.getOpenGroups().contains(openGroup1));

        assertNotNull(resultRequest.getRequestedRange());
        assertEquals(rangeLength, resultRequest.getRequestedRange().getLength());
        assertEquals(rangeOffset, resultRequest.getRequestedRange().getOffset());

        assertEquals(3, resultRequest.getMappings().size());
        assertEquals(queryId0, resultRequest.getMappings().get(0).getQueryId());
        assertEquals(queryId1, resultRequest.getMappings().get(1).getQueryId());
        assertEquals(queryId2, resultRequest.getMappings().get(2).getQueryId());
    }
}
