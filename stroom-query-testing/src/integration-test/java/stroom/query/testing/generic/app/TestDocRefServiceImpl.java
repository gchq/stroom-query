package stroom.query.testing.generic.app;

import stroom.query.audit.ExportDTO;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.DocRefService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TestDocRefServiceImpl implements DocRefService<TestDocRefEntity> {

    private static Map<String, TestDocRefEntity> data = new HashMap<>();

    public static void eraseAllData() {
        data.clear();
    }

    @Override
    public String getType() {
        return TestDocRefEntity.TYPE;
    }

    @Override
    public List<TestDocRefEntity> getAll(final ServiceUser user) throws Exception {
        return new ArrayList<>(data.values());
    }

    @Override
    public Optional<TestDocRefEntity> get(final ServiceUser user,
                                          final String uuid) throws Exception {
        return Optional.ofNullable(data.get(uuid));
    }

    @Override
    public Optional<TestDocRefEntity> createDocument(final ServiceUser user,
                                                     final String uuid,
                                                     final String name) throws Exception {
        final Long now = System.currentTimeMillis();
        data.put(uuid, new TestDocRefEntity.Builder()
                .uuid(uuid)
                .name(name)
                .createUser(user.getName())
                .createTime(now)
                .updateUser(user.getName())
                .updateTime(now)
                .build());

        return get(user, uuid);
    }

    @Override
    public Optional<TestDocRefEntity> update(final ServiceUser user,
                                             final String uuid,
                                             final TestDocRefEntity updatedConfig) throws Exception {
        return get(user, uuid)
                .map(d -> new TestDocRefEntity.Builder(d)
                        .updateTime(System.currentTimeMillis())
                        .updateUser(user.getName())
                        .indexName(updatedConfig.getIndexName())
                        .build());
    }

    @Override
    public Optional<TestDocRefEntity> copyDocument(final ServiceUser user,
                                                   final String originalUuid,
                                                   final String copyUuid) throws Exception {
        final TestDocRefEntity existing = data.get(originalUuid);
        if (null != existing) {
            createDocument(user, copyUuid, existing.getName());
            return update(user, copyUuid, existing);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<TestDocRefEntity> moveDocument(final ServiceUser user,
                                                   final String uuid) throws Exception {
        return get(user, uuid);
    }

    @Override
    public Optional<TestDocRefEntity> renameDocument(final ServiceUser user,
                                                     final String uuid,
                                                     final String name) throws Exception {
        return get(user, uuid)
                .map(d -> new TestDocRefEntity.Builder(d)
                        .updateTime(System.currentTimeMillis())
                        .updateUser(user.getName())
                        .name(name)
                        .build());
    }

    @Override
    public Optional<Boolean> deleteDocument(final ServiceUser user,
                                            final String uuid) throws Exception {
        if (data.containsKey(uuid)) {
            data.remove(uuid);
            return Optional.of(Boolean.TRUE);
        } else {
            return Optional.of(Boolean.FALSE);
        }
    }

    @Override
    public ExportDTO exportDocument(final ServiceUser user,
                                    final String uuid) throws Exception {
        return get(user, uuid)
                .map(d -> new ExportDTO.Builder()
                        .value(DocRefEntity.NAME, d.getName())
                        .value(TestDocRefEntity.INDEX_NAME, d.getIndexName())
                        .build())
                .orElse(new ExportDTO.Builder()
                        .message(String.format("Could not find test doc ref: %s", uuid))
                        .build());
    }

    @Override
    public Optional<TestDocRefEntity> importDocument(final ServiceUser user,
                                                     final String uuid,
                                                     final String name,
                                                     final Boolean confirmed,
                                                     final Map<String, String> dataMap) throws Exception {
        if (confirmed) {
            final Optional<TestDocRefEntity> index = createDocument(user, uuid, name);

            if (index.isPresent()) {
                final TestDocRefEntity indexConfig = index.get();
                indexConfig.setIndexName(dataMap.get(TestDocRefEntity.INDEX_NAME));
                return update(user, uuid, indexConfig);
            } else {
                return Optional.empty();
            }
        } else {
            return get(user, uuid)
                    .map(d -> Optional.<TestDocRefEntity>empty())
                    .orElse(Optional.of(new TestDocRefEntity.Builder()
                            .uuid(uuid)
                            .name(name)
                            .indexName(dataMap.get(TestDocRefEntity.INDEX_NAME))
                            .build()));
        }
    }
}
