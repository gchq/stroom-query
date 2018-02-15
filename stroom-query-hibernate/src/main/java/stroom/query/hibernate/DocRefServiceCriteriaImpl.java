package stroom.query.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.query.audit.ExportDTO;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.service.DocRefService;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class DocRefServiceCriteriaImpl<
        DOC_REF_ENTITY extends DocRefHibernateEntity,
        DOC_REF_BUILDER extends DocRefHibernateEntity.BaseBuilder<DOC_REF_ENTITY, ?>>
        implements DocRefService<DOC_REF_ENTITY> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocRefServiceCriteriaImpl.class);

    private SessionFactory database;
    
    private final Class<DOC_REF_ENTITY> docRefEntityClass;

    protected abstract DOC_REF_BUILDER createDocumentBuilder();

    protected abstract DOC_REF_BUILDER copyEntity(DOC_REF_ENTITY original);

    protected abstract DOC_REF_BUILDER createImport(Map<String, String> dataMap);

    @Inject
    public DocRefServiceCriteriaImpl(final SessionFactory database,
                                     final Class<DOC_REF_ENTITY> docRefEntityClass) { 
        this.database = database;
        this.docRefEntityClass = docRefEntityClass;
    }

    @Override
    public List<DOC_REF_ENTITY> getAll(final ServiceUser user) throws Exception {
        try (final Session session = database.openSession()){
            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaQuery<DOC_REF_ENTITY> cq = cb.createQuery(docRefEntityClass);
            final Root<DOC_REF_ENTITY> root = cq.from(docRefEntityClass);

            cq.select(root);
            cq.distinct(true);

            return session.createQuery(cq).getResultList();
        } catch (final Exception e) {
            LOGGER.warn("Failed to get index list", e);
            throw e;
        }
    }

    @Override
    public Optional<DOC_REF_ENTITY> get(final ServiceUser user, final String uuid) throws Exception {
        try (final Session session = database.openSession()){
            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaQuery<DOC_REF_ENTITY> cq = cb.createQuery(docRefEntityClass);
            final Root<DOC_REF_ENTITY> root = cq.from(docRefEntityClass);

            cq.select(root);
            cq.where(cb.equal(root.get(DocRefHibernateEntity.UUID), uuid));
            cq.distinct(true);

            final DOC_REF_ENTITY entity = session.createQuery(cq).getSingleResult();
            return Optional.of(entity);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (final Exception e) {
            LOGGER.warn("Failed to get index list", e);
            throw e;
        }
    }

    @Override
    public Optional<DOC_REF_ENTITY> createDocument(final ServiceUser user,
                                                   final String uuid,
                                                   final String name) throws Exception {
        Transaction tx;

        try (final Session session = database.openSession()) {
            tx = session.beginTransaction();

            final Long now = System.currentTimeMillis();

            final DOC_REF_ENTITY annotation = createDocumentBuilder()
                    .uuid(uuid)
                    .name(name)
                    .createTime(now)
                    .createUser(user.getName())
                    .updateTime(now)
                    .updateUser(user.getName())
                    .build();

            session.persist(annotation);

            tx.commit();

            return Optional.of(annotation);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (final Exception e) {
            LOGGER.warn("Failed to get create index", e);

            throw e;
        }
    }

    @Override
    public Optional<DOC_REF_ENTITY> update(final ServiceUser user,
                                           final String uuid,
                                           final DOC_REF_ENTITY updatedConfig) throws Exception {
        Transaction tx;

        try (final Session session = database.openSession()) {
            final Long now = System.currentTimeMillis();

            tx = session.beginTransaction();

            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaUpdate<DOC_REF_ENTITY> cq = cb.createCriteriaUpdate(docRefEntityClass);
            final Root<DOC_REF_ENTITY> root = cq.from(docRefEntityClass);

            cq.set(root.get(DocRefHibernateEntity.NAME), updatedConfig.getName());
            cq.set(root.get(DocRefHibernateEntity.UPDATE_USER), user.getName());
            cq.set(root.get(DocRefHibernateEntity.UPDATE_TIME), now);

            // include all the specific values
            exportValues(updatedConfig).forEach((k, v) -> cq.set(root.get(k), v));

            cq.where(cb.equal(root.get(DocRefHibernateEntity.UUID), uuid));

            int rowsAffected = session.createQuery(cq).executeUpdate();

            if (rowsAffected == 0) {
                throw new Exception("Zero rows affected by the update");
            }

            tx.commit();

            return get(user, uuid);

        } catch (NoResultException e) {
            return Optional.empty();
        } catch (final Exception e) {
            LOGGER.warn("Failed to get update annotation", e);
            throw e;
        }
    }

    @Override
    public Optional<DOC_REF_ENTITY> copyDocument(final ServiceUser user,
                                                 final String originalUuid,
                                                 final String copyUuid) throws Exception {
        Transaction tx;

        try (final Session session = database.openSession()) {
            tx = session.beginTransaction();

            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaQuery<DOC_REF_ENTITY> cq = cb.createQuery(docRefEntityClass);
            final Root<DOC_REF_ENTITY> root = cq.from(docRefEntityClass);

            cq.select(root);
            cq.where(cb.equal(root.get(DocRefHibernateEntity.UUID), originalUuid));
            cq.distinct(true);

            final DOC_REF_ENTITY original = session.createQuery(cq).getSingleResult();

            final Long now = System.currentTimeMillis();

            final DOC_REF_ENTITY annotation = copyEntity(original)
                    .uuid(copyUuid)
                    .name(original.getName())
                    .createTime(now)
                    .createUser(user.getName())
                    .updateTime(now)
                    .updateUser(user.getName())
                    .build();
            session.persist(annotation);

            tx.commit();

            return Optional.of(annotation);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (final Exception e) {
            LOGGER.warn("Failed to get create index", e);

            throw e;
        }
    }

    @Override
    public Optional<DOC_REF_ENTITY> moveDocument(final ServiceUser user,
                                                 final String uuid) throws Exception {

        // Nothing to worry about here
        return get(user, uuid);
    }

    @Override
    public Optional<DOC_REF_ENTITY> renameDocument(final ServiceUser user,
                                                   final String uuid,
                                                   final String name) throws Exception {
        Transaction tx;

        try (final Session session = database.openSession()) {
            final Long now = System.currentTimeMillis();

            tx = session.beginTransaction();

            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaUpdate<DOC_REF_ENTITY> cq = cb.createCriteriaUpdate(docRefEntityClass);
            final Root<DOC_REF_ENTITY> root = cq.from(docRefEntityClass);

            cq.set(root.get(DocRefHibernateEntity.NAME), name);
            cq.set(root.get(DocRefHibernateEntity.UPDATE_USER), user.getName());
            cq.set(root.get(DocRefHibernateEntity.UPDATE_TIME), now);

            cq.where(cb.equal(root.get(DocRefHibernateEntity.UUID), uuid));

            int rowsAffected = session.createQuery(cq).executeUpdate();

            if (rowsAffected == 0) {
                throw new Exception("Zero rows affected by the update");
            }

            tx.commit();

            return get(user, uuid);

        } catch (NoResultException e) {
            return Optional.empty();
        } catch (final Exception e) {
            LOGGER.warn("Failed to get update annotation", e);
            throw e;
        }
    }

    @Override
    public Optional<Boolean> deleteDocument(final ServiceUser user,
                                            final String uuid) throws Exception {
        Transaction tx;

        try (final Session session = database.openSession()) {
            tx = session.beginTransaction();

            final CriteriaBuilder cb = session.getCriteriaBuilder();

            final CriteriaDelete<DOC_REF_ENTITY> cq = cb.createCriteriaDelete(docRefEntityClass);
            final Root<DOC_REF_ENTITY> root = cq.from(docRefEntityClass);
            cq.where(cb.equal(root.get(DocRefHibernateEntity.UUID), uuid));

            int rowsAffected = session.createQuery(cq).executeUpdate();
            if (rowsAffected == 0) {
                throw new Exception("Zero rows affected by the update");
            }

            tx.commit();

            return Optional.of(Boolean.TRUE);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (final Exception e) {
            LOGGER.warn("Failed to get create annotation", e);
            throw e;
        }
    }

    @Override
    public ExportDTO exportDocument(final ServiceUser user,
                                    final String uuid) throws Exception {
        final Optional<DOC_REF_ENTITY> optionalIndex = get(user, uuid);

        return optionalIndex.map(index -> new ExportDTO.Builder()
                    .value(DocRefEntity.NAME, index.getName())
                    .values(exportValues(index))
                    .build())
                .orElse(new ExportDTO.Builder()
                        .message("could not find document")
                        .build());
    }

    protected abstract Map<String, Object> exportValues(DOC_REF_ENTITY docRefEntity);

    @Override
    public Optional<DOC_REF_ENTITY> importDocument(final ServiceUser user,
                                                   final String uuid,
                                                   final String name,
                                                   final Boolean confirmed,
                                                   final Map<String, String> dataMap) throws Exception {
        if (confirmed) {
            return createDocument(user, uuid, name);
        } else {
            final Optional<DOC_REF_ENTITY> existing = get(user, uuid);

            if (null != existing) {
                return Optional.empty();
            } else {
                return Optional.of(createImport(dataMap)
                        .uuid(uuid)
                        .name(name)
                        .build());
            }
        }
    }
}
