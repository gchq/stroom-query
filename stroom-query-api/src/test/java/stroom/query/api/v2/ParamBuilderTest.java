package stroom.query.api.v2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParamBuilderTest {
    @Test
    public void doesBuild() {
        final String key = "someKey";
        final String value = "someValue";

        final Param param = new Param.Builder()
                .key(key)
                .value(value)
                .build();

        assertEquals(key, param.getKey());
        assertEquals(value, param.getValue());
    }
}
