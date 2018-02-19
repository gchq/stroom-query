package stroom.query.jooq;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import stroom.query.audit.ExportDTO;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.DocRefService;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.inline;
import static org.jooq.impl.DSL.table;

public abstract class DocRefServiceJooqImpl<
        DOC_REF_ENTITY extends DocRefJooqEntity,
        DOC_REF_BUILDER extends DocRefJooqEntity.BaseBuilder<DOC_REF_ENTITY, ?>>
        implements DocRefService<DOC_REF_ENTITY> {

    protected abstract DOC_REF_BUILDER createDocumentBuilder(Record record);

    protected abstract Map<Field<?>, Object> getMappedFields(DOC_REF_ENTITY docRefEntity);

    private final DSLContext database;

    private final Table<Record> table;

    @Inject
    public DocRefServiceJooqImpl(final String tableName,
                                 final Configuration jooqConfig) {
        this.table = table(tableName);
        this.database = DSL.using(jooqConfig);
    }


    @Override
    public List<DOC_REF_ENTITY> getAll(final ServiceUser user) throws Exception {
        return null;
    }

    private DOC_REF_ENTITY convertRecord(final Record record) {
        return createDocumentBuilder(record)
                .uuid(record.getValue(DocRefJooqEntity.UUID_FIELD))
                .name(record.getValue(DocRefJooqEntity.NAME_FIELD))
                .createUser(record.getValue(DocRefJooqEntity.CREATE_USER_FIELD))
                .createTime(record.getValue(DocRefJooqEntity.CREATE_TIME_FIELD).longValue())
                .updateUser(record.getValue(DocRefJooqEntity.UPDATE_USER_FIELD))
                .updateTime(record.getValue(DocRefJooqEntity.UPDATE_TIME_FIELD).longValue())
                .build();
    }

    @Override
    public Optional<DOC_REF_ENTITY> get(final ServiceUser user,
                                        final String uuid) throws Exception {

        return database.transactionResult(configuration -> {
            final Record record = database.select()
                    .from(table)
                    .where(DocRefJooqEntity.UUID_FIELD.equal(uuid))
                    .fetchOne();

            return Optional.ofNullable(record).map(this::convertRecord);
        });
    }

    @Override
    public Optional<DOC_REF_ENTITY> createDocument(final ServiceUser user,
                                                   final String uuid,
                                                   final String name) throws Exception {
        final ULong now = ULong.valueOf(System.currentTimeMillis());

        return database.transactionResult(configuration -> {

            DSL.using(configuration)
                    .insertInto(table)
                    .columns(DocRefJooqEntity.UUID_FIELD,
                            DocRefJooqEntity.NAME_FIELD,
                            DocRefJooqEntity.CREATE_USER_FIELD,
                            DocRefJooqEntity.CREATE_TIME_FIELD,
                            DocRefJooqEntity.UPDATE_USER_FIELD,
                            DocRefJooqEntity.UPDATE_TIME_FIELD)
                    .values(uuid, name, user.getName(), now, user.getName(), now)
                    .execute();

            final Record record = DSL.using(configuration)
                    .select()
                    .from(table)
                    .where(DocRefJooqEntity.UUID_FIELD.equal(uuid))
                    .fetchOne();

            return Optional.ofNullable(record).map(this::convertRecord);
        });
    }

    @Override
    public Optional<DOC_REF_ENTITY> update(final ServiceUser user,
                                           final String uuid,
                                           final DOC_REF_ENTITY updated) throws Exception {
        final ULong now = ULong.valueOf(System.currentTimeMillis());

        return database.transactionResult(configuration -> {

            final UpdateSetMoreStep<Record> updateStmt = DSL.using(configuration)
                    .update(table)
                    .set(DocRefJooqEntity.UPDATE_USER_FIELD, user.getName())
                    .set(DocRefJooqEntity.UPDATE_TIME_FIELD, now);

            final Map<Field<?>, Object> fields = getMappedFields(updated);

            fields.forEach((field, o) -> updateStmt.set((Field<Object>) field, o));

            updateStmt
                    .where(DocRefJooqEntity.UUID_FIELD.equal(uuid))
                    .execute();

            final Record record = DSL.using(configuration)
                    .select()
                    .from(table)
                    .where(DocRefJooqEntity.UUID_FIELD.equal(uuid))
                    .fetchOne();

            return Optional.ofNullable(record).map(this::convertRecord);
        });
    }

    @Override
    public Optional<DOC_REF_ENTITY> copyDocument(final ServiceUser user,
                                                 final String originalUuid,
                                                 final String copyUuid) throws Exception {
        return database.transactionResult(configuration -> {
            DSL.using(configuration)
                    .insertInto(table, DocRefJooqEntity.UUID_FIELD,
                            DocRefJooqEntity.NAME_FIELD,
                            DocRefJooqEntity.CREATE_USER_FIELD,
                            DocRefJooqEntity.CREATE_TIME_FIELD,
                            DocRefJooqEntity.UPDATE_USER_FIELD,
                            DocRefJooqEntity.UPDATE_TIME_FIELD)
                    .select(DSL.using(configuration)
                            .select(inline(copyUuid),
                                    DocRefJooqEntity.NAME_FIELD,
                                    DocRefJooqEntity.CREATE_USER_FIELD,
                                    DocRefJooqEntity.CREATE_TIME_FIELD,
                                    DocRefJooqEntity.UPDATE_USER_FIELD,
                                    DocRefJooqEntity.UPDATE_TIME_FIELD)
                            .from(table)
                            .where(DocRefJooqEntity.UUID_FIELD.equal(originalUuid))
                    )
                    .execute();

            final Record record = DSL.using(configuration)
                    .select()
                    .from(table)
                    .where(DocRefJooqEntity.UUID_FIELD.equal(copyUuid))
                    .fetchOne();

            return Optional.ofNullable(record).map(this::convertRecord);
        });
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
        final ULong now = ULong.valueOf(System.currentTimeMillis());

        return database.transactionResult(configuration -> {
            DSL.using(configuration)
                    .update(table)
                    .set(DocRefJooqEntity.UPDATE_USER_FIELD, user.getName())
                    .set(DocRefJooqEntity.UPDATE_TIME_FIELD, now)
                    .set(DocRefJooqEntity.NAME_FIELD, name)
                    .where(DocRefJooqEntity.UUID_FIELD.equal(uuid))
                    .execute();

            final Record record = DSL.using(configuration)
                    .select()
                    .from(table)
                    .where(DocRefJooqEntity.UUID_FIELD.equal(uuid))
                    .fetchOne();

            return Optional.ofNullable(record).map(this::convertRecord);
        });
    }

    @Override
    public Optional<Boolean> deleteDocument(final ServiceUser user,
                                            final String uuid) throws Exception {
        return database.transactionResult(configuration -> {

            final int rowsDeleted = database.deleteFrom(table)
                    .where(DocRefJooqEntity.UUID_FIELD.equal(uuid))
                    .execute();

            return Optional.of(rowsDeleted == 1);
        });
    }

    @Override
    public ExportDTO exportDocument(final ServiceUser user,
                                    final String uuid) throws Exception {
        return database.transactionResult(configuration -> {

            final Record record = DSL.using(configuration)
                    .select()
                    .from(table)
                    .where(DocRefJooqEntity.UUID_FIELD.equal(uuid))
                    .fetchOne();

            return Optional.ofNullable(record)
                    .map(this::convertRecord)
                    .map(e -> {

                        final Map<String, Object> fieldValues = new HashMap<>();
                        getMappedFields(e)
                                .forEach((field, o) -> fieldValues.put(field.getName(), o));

                        return new ExportDTO.Builder()
                            .value(DocRefEntity.NAME, e.getName())
                            .values(fieldValues)
                            .build();
                    })
                    .orElse(new ExportDTO.Builder()
                            .message("could not find document")
                            .build());
        });
    }

    @Override
    public Optional<DOC_REF_ENTITY> importDocument(final ServiceUser user,
                                                   final String uuid,
                                                   final String name,
                                                   final Boolean confirmed,
                                                   final Map<String, String> dataMap) throws Exception {

        return database.transactionResult(configuration -> {

            if (confirmed) {
                final ULong now = ULong.valueOf(System.currentTimeMillis());

                DSL.using(configuration)
                        .insertInto(table)
                        .columns(DocRefJooqEntity.UUID_FIELD,
                                DocRefJooqEntity.NAME_FIELD,
                                DocRefJooqEntity.CREATE_USER_FIELD,
                                DocRefJooqEntity.CREATE_TIME_FIELD,
                                DocRefJooqEntity.UPDATE_USER_FIELD,
                                DocRefJooqEntity.UPDATE_TIME_FIELD)
                        .values(uuid, name, user.getName(), now, user.getName(), now)
                        .execute();

                final UpdateSetMoreStep updateStmt = DSL.using(configuration).update(table)
                        .set(DocRefJooqEntity.NAME_FIELD, name);

                dataMap.forEach((fieldName, fieldValue) -> updateStmt.set(field(fieldName), fieldValue));

                updateStmt.execute();
            }

            final Record record = DSL.using(configuration)
                    .select()
                    .from(table)
                    .where(DocRefJooqEntity.UUID_FIELD.equal(uuid))
                    .fetchOne();

            return Optional.ofNullable(record).map(this::convertRecord);
        });
    }
}
