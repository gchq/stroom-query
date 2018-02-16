package stroom.query.jooq;

import stroom.query.audit.ExportDTO;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.DocRefService;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class DocRefServiceJooqImpl<
        DOC_REF_ENTITY extends DocRefJooqEntity,
        DOC_REF_BUILDER extends DocRefJooqEntity.BaseBuilder<DOC_REF_ENTITY, ?>>
        implements DocRefService<DOC_REF_ENTITY> {
    private final Class<DOC_REF_ENTITY> docRefEntityClass;

    protected abstract DOC_REF_BUILDER createDocumentBuilder();

    protected abstract DOC_REF_BUILDER copyEntity(DOC_REF_ENTITY original);

    protected abstract DOC_REF_BUILDER createImport(Map<String, String> dataMap);

    protected abstract Map<String, Object> exportValues(DOC_REF_ENTITY docRefEntity);

    @Inject
    public DocRefServiceJooqImpl(final Class<DOC_REF_ENTITY> docRefEntityClass) {
        this.docRefEntityClass = docRefEntityClass;
    }


    @Override
    public List<DOC_REF_ENTITY> getAll(ServiceUser user) throws Exception {
        return null;
    }

    @Override
    public Optional<DOC_REF_ENTITY> get(ServiceUser user, String uuid) throws Exception {
        return Optional.empty();
    }

    @Override
    public Optional<DOC_REF_ENTITY> createDocument(ServiceUser user, String uuid, String name) throws Exception {
        return Optional.empty();
    }

    @Override
    public Optional<DOC_REF_ENTITY> update(ServiceUser user, String uuid, DOC_REF_ENTITY updatedConfig) throws Exception {
        return Optional.empty();
    }

    @Override
    public Optional<DOC_REF_ENTITY> copyDocument(ServiceUser user, String originalUuid, String copyUuid) throws Exception {
        return Optional.empty();
    }

    @Override
    public Optional<DOC_REF_ENTITY> moveDocument(ServiceUser user, String uuid) throws Exception {
        return Optional.empty();
    }

    @Override
    public Optional<DOC_REF_ENTITY> renameDocument(ServiceUser user, String uuid, String name) throws Exception {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> deleteDocument(ServiceUser user, String uuid) throws Exception {
        return Optional.empty();
    }

    @Override
    public ExportDTO exportDocument(ServiceUser user, String uuid) throws Exception {
        return null;
    }

    @Override
    public Optional<DOC_REF_ENTITY> importDocument(ServiceUser user, String uuid, String name, Boolean confirmed, Map<String, String> dataMap) throws Exception {
        return Optional.empty();
    }
}
