package stroom.query.testing.hibernate.app;

import org.hibernate.SessionFactory;
import stroom.query.hibernate.DocRefServiceCriteriaImpl;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class TestDocRefServiceCriteriaImpl extends DocRefServiceCriteriaImpl<TestDocRefHibernateEntity, TestDocRefHibernateEntity.Builder> {

    @Inject
    public TestDocRefServiceCriteriaImpl(final SessionFactory database) {
        super(database, TestDocRefHibernateEntity.class);
    }

    @Override
    protected TestDocRefHibernateEntity.Builder createDocumentBuilder() {
        return new TestDocRefHibernateEntity.Builder();
    }

    @Override
    protected TestDocRefHibernateEntity.Builder copyEntity(final TestDocRefHibernateEntity original) {
        return new TestDocRefHibernateEntity.Builder()
                .clanName(original.getClanName());
    }

    @Override
    protected TestDocRefHibernateEntity.Builder createImport(final Map<String, String> dataMap) {
        return new TestDocRefHibernateEntity.Builder()
                .clanName(dataMap.get(TestDocRefHibernateEntity.CLAN_NAME));
    }

    @Override
    protected Map<String, Object> exportValues(final TestDocRefHibernateEntity entity) {
        return new HashMap<String, Object>()
        {
            {
                put(TestDocRefHibernateEntity.CLAN_NAME, entity.getClanName());
            }
        };
    }

    @Override
    public String getType() {
        return TestDocRefHibernateEntity.TYPE;
    }
}
