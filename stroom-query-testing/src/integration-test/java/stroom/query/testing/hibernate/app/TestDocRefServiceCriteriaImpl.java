package stroom.query.testing.hibernate.app;

import org.hibernate.SessionFactory;
import stroom.query.hibernate.DocRefServiceCriteriaImpl;

import javax.inject.Inject;

public class TestDocRefServiceCriteriaImpl extends DocRefServiceCriteriaImpl<TestDocRefHibernateEntity> {

    @Inject
    public TestDocRefServiceCriteriaImpl(final SessionFactory database) {
        super(TestDocRefHibernateEntity.TYPE, TestDocRefHibernateEntity.class,
                dataMap -> new TestDocRefHibernateEntity.Builder()
                        .clanName(dataMap.getValue(TestDocRefHibernateEntity.CLAN_NAME, String.class).orElse(null)),
                (entity, consumer) -> consumer.setValue(TestDocRefHibernateEntity.CLAN_NAME, entity.getClanName()),
                database);
    }
}
