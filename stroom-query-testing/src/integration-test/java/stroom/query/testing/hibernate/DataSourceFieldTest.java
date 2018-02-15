package stroom.query.testing.hibernate;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.query.audit.model.IsDataSourceField;
import stroom.query.audit.model.QueryableEntity;
import stroom.query.testing.hibernate.app.TestQueryableEntity;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class DataSourceFieldTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceFieldTest.class);

    @Test
    public void test() {
        final Set<IsDataSourceField> annotations = QueryableEntity.getFields(TestQueryableEntity.class).collect(Collectors.toSet());
        LOGGER.info("Annotations Found: " + annotations);
        assertTrue(annotations.size() > 0);
    }
}
