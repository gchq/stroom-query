package stroom.query.testing.hibernate.app;

import org.hibernate.SessionFactory;
import stroom.query.hibernate.DocRefServiceCriteriaImpl;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class TestDocRefServiceCriteriaImpl extends DocRefServiceCriteriaImpl<TestDocRefHibernateEntity, TestDocRefHibernateEntity.Builder> {

    @Inject
    public TestDocRefServiceCriteriaImpl(final SessionFactory database) {
        super(database, TestDocRefHibernateEntity.class);
    }

    @Override
    protected TestDocRefHibernateEntity.Builder createImport(final GetValue dataMap) {
        return new TestDocRefHibernateEntity.Builder()
                .clanName(dataMap.getValue(TestDocRefHibernateEntity.CLAN_NAME, String.class).orElse(null));
    }

    @Override
    protected void exportValues(final TestDocRefHibernateEntity entity,
                                final SetValue consumer) {
        consumer.setValue(TestDocRefHibernateEntity.CLAN_NAME, entity.getClanName());
    }

    @Override
    public String getType() {
        return TestDocRefHibernateEntity.TYPE;
    }
}
