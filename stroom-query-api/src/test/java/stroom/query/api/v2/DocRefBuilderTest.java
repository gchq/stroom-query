package stroom.query.api.v2;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class DocRefBuilderTest {
    @Test
    public void doesBuild() {
        final String name = "someName";
        final String type = "someType";
        final String uuid = UUID.randomUUID().toString();

        final DocRef docRef = new DocRef.Builder()
                .name(name)
                .type(type)
                .uuid(uuid)
                .build();

        assertEquals(name, docRef.getName());
        assertEquals(type, docRef.getType());
        assertEquals(uuid, docRef.getUuid());
    }
}
