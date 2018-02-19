package stroom.query.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.query.audit.ExportDTO;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.DocRefService;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public abstract class DocRefServiceCriteriaImpl<
        DOC_REF_ENTITY extends DocRefHibernateEntity,
        DOC_REF_BUILDER extends DocRefHibernateEntity.BaseBuilder<DOC_REF_ENTITY, ?>>
        implements DocRefService<DOC_REF_ENTITY> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocRefServiceCriteriaImpl.class);

    private SessionFactory database;
    
    private final Class<DOC_REF_ENTITY> docRefEntityClass;

    @FunctionalInterface
    protected interface GetValue {
        <T> Optional<T> getValue(String fieldName, Class<T> clazz);

        static GetValue empty() {
            return new GetValue() {
                @Override
                public <T> Optional<T> getValue(String fieldName, Class<T> clazz) {
                    return Optional.empty();
                }
            };
        }

        static GetValue fromMap(final Map<String, ?> map) {

            return new GetValue() {
                @Override
                public <T> Optional<T> getValue(String fieldName, Class<T> clazz) {
                    final Object value = map.get(fieldName);

                    return Optional.ofNullable(value)
                            .filter(clazz::isInstance)
                            .map((d) -> (T) d);
                }
            };
        }
    }

    @FunctionalInterface
    protected interface SetValue {
        void setValue(final String fieldName, final Object fieldValue);
    }

    protected abstract DOC_REF_BUILDER createImport(GetValue dataMap);

    protected abstract void exportValues(DOC_REF_ENTITY docRefEntity, SetValue consumer);

    private Map<String, Object> exportValuesToMap(DOC_REF_ENTITY docRefEntity) {
        final Map<String, Object> map = new HashMap<>();
        exportValues(docRefEntity, map::put);
        return map;
    }

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

            final DOC_REF_ENTITY entity = createImport(GetValue.empty())
                    .uuid(uuid)
                    .name(name)
                    .createTime(now)
                    .createUser(user.getName())
                    .updateTime(now)
                    .updateUser(user.getName())
                    .build();

            session.persist(entity);

            tx.commit();

            return Optional.of(entity);
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
            exportValues(updatedConfig, (k, v) -> cq.set(root.get(k), v));

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
            LOGGER.warn("Failed to get update entity", e);
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

            final Map<String, ?> exportedValues = exportValuesToMap(original);
            final DOC_REF_ENTITY entity = createImport(GetValue.fromMap(exportedValues))
                    .uuid(copyUuid)
                    .name(original.getName())
                    .createTime(now)
                    .createUser(user.getName())
                    .updateTime(now)
                    .updateUser(user.getName())
                    .build();
            session.persist(entity);

            tx.commit();

            return Optional.of(entity);
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
            LOGGER.warn("Failed to get update entity", e);
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
            LOGGER.warn("Failed to get create entity", e);
            throw e;
        }
    }

    @Override
    public ExportDTO exportDocument(final ServiceUser user,
                                    final String uuid) throws Exception {
        final Optional<DOC_REF_ENTITY> optionalIndex = get(user, uuid);

        return optionalIndex.map(index -> new ExportDTO.Builder()
                    .value(DocRefEntity.NAME, index.getName())
                    .values(exportValuesToMap(index))
                    .build())
                .orElse(new ExportDTO.Builder()
                        .message("could not find document")
                        .build());
    }

    @Override
    public Optional<DOC_REF_ENTITY> importDocument(final ServiceUser user,
                                                   final String uuid,
                                                   final String name,
                                                   final Boolean confirmed,
                                                   final Map<String, String> dataMap) throws Exception {

        Transaction tx;

        try (final Session session = database.openSession()) {
            if (confirmed) {
                tx = session.beginTransaction();

                final Long now = System.currentTimeMillis();

                final DOC_REF_ENTITY entity = createImport(GetValue.fromMap(dataMap))
                        .uuid(uuid)
                        .name(name)
                        .createTime(now)
                        .createUser(user.getName())
                        .updateTime(now)
                        .updateUser(user.getName())
                        .build();

                session.persist(entity);

                tx.commit();

                return Optional.of(entity);
            } else {
                final CriteriaBuilder cb = session.getCriteriaBuilder();
                final CriteriaQuery<DOC_REF_ENTITY> cq = cb.createQuery(docRefEntityClass);
                final Root<DOC_REF_ENTITY> root = cq.from(docRefEntityClass);

                cq.select(root);
                cq.where(cb.equal(root.get(DocRefHibernateEntity.UUID), uuid));
                cq.distinct(true);

                final DOC_REF_ENTITY entity = session.createQuery(cq).getSingleResult();
                return Optional.of(entity);
            }
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (final Exception e) {
            LOGGER.warn("Failed to get create index", e);

            throw e;
        }
    }
}
