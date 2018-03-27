package stroom.query.jooq;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import stroom.query.audit.ExportDTO;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.DocRefService;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.inline;

public class DocRefServiceJooqImpl<DOC_REF_ENTITY extends DocRefJooqEntity>
        implements DocRefService<DOC_REF_ENTITY> {

    @FunctionalInterface
    protected interface ImportValue {
        <T> Optional<T> getValue(Field<T> fieldName);

        static ImportValue ofRecord(final Record record) {
            return new ImportValue() {
                @Override
                public <T> Optional<T> getValue(Field<T> field) {
                    return Optional.ofNullable(record.getValue(field));
                }
            };
        }
    }

    @FunctionalInterface
    protected interface ValueImporter<
            E extends DocRefJooqEntity,
            B extends DocRefJooqEntity.BaseBuilder<E, ?>> {
        B importValues(ImportValue dataMap);
    }


    @FunctionalInterface
    protected interface ExportValue {
        <T> void setValue(final Field<T> field, final T fieldValue);
    }

    @FunctionalInterface
    protected interface ValueExporter<E extends DocRefJooqEntity> {
        void exportValues(E docRefEntity, ExportValue consumer);
    }

    private final String type;

    private final Class<DOC_REF_ENTITY> docRefEntityClass;

    private final DSLContext database;

    private final Table<Record> table;

    private final ValueImporter<DOC_REF_ENTITY, DocRefJooqEntity.BaseBuilder<DOC_REF_ENTITY, ?>> valueImporter;

    private final ValueExporter<DOC_REF_ENTITY> valueExporter;

    @Inject
    public DocRefServiceJooqImpl(final String type,
                                 final ValueImporter<DOC_REF_ENTITY, DocRefJooqEntity.BaseBuilder<DOC_REF_ENTITY, ?>> valueImporter,
                                 final ValueExporter<DOC_REF_ENTITY> valueExporter,
                                 final Class<DOC_REF_ENTITY> docRefEntityClass,
                                 final DSLContext database) {
        this.type = type;
        this.docRefEntityClass = docRefEntityClass;

        this.table = Optional.ofNullable(docRefEntityClass.getAnnotation(JooqEntity.class))
                .map(JooqEntity::tableName)
                .map(DSL::table)
                .orElseThrow(() -> new IllegalArgumentException("The Document Entity Class must be annotated with JooqEntity"));

        this.database = database;
        this.valueImporter = valueImporter;
        this.valueExporter = valueExporter;
    }


    @Override
    public String getType() {
        return type;
    }

    @Override
    public List<DOC_REF_ENTITY> getAll(final ServiceUser user) {
        return database.transactionResult(configuration -> DSL.using(configuration)
                .select()
                .from(table)
                .fetch()
                .into(docRefEntityClass));
    }

    private DOC_REF_ENTITY convertRecord(final Record record) {
        return valueImporter.importValues(ImportValue.ofRecord(record))
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
                                        final String uuid) {

        return database.transactionResult(configuration -> {
            final Record record = DSL.using(configuration)
                    .select()
                    .from(table)
                    .where(DocRefJooqEntity.UUID_FIELD.equal(uuid))
                    .fetchOne();

            return Optional.ofNullable(record).map(this::convertRecord);
        });
    }

    @Override
    public Optional<DOC_REF_ENTITY> createDocument(final ServiceUser user,
                                                   final String uuid,
                                                   final String name) {

        return database.transactionResult(configuration -> {
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

            final DOC_REF_ENTITY result = DSL.using(configuration)
                    .select()
                    .from(table)
                    .where(DocRefJooqEntity.UUID_FIELD.equal(uuid))
                    .fetchOneInto(docRefEntityClass);

            return Optional.ofNullable(result);
        });
    }

    @Override
    public Optional<DOC_REF_ENTITY> update(final ServiceUser user,
                                           final String uuid,
                                           final DOC_REF_ENTITY updated) {
        final ULong now = ULong.valueOf(System.currentTimeMillis());

        return database.transactionResult(configuration -> {

            final UpdateSetMoreStep<Record> updateStmt = DSL.using(configuration)
                    .update(table)
                    .set(DocRefJooqEntity.UPDATE_USER_FIELD, user.getName())
                    .set(DocRefJooqEntity.UPDATE_TIME_FIELD, now);

            valueExporter.exportValues(updated, updateStmt::set);

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
                                                 final String copyUuid) {
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
                                                 final String uuid) {
        // Nothing to worry about here
        return get(user, uuid);
    }

    @Override
    public Optional<DOC_REF_ENTITY> renameDocument(final ServiceUser user,
                                                   final String uuid,
                                                   final String name) {
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
                                            final String uuid) {
        return database.transactionResult(configuration -> {

            final int rowsDeleted = DSL.using(configuration)
                    .deleteFrom(table)
                    .where(DocRefJooqEntity.UUID_FIELD.equal(uuid))
                    .execute();

            return Optional.of(rowsDeleted == 1);
        });
    }

    @Override
    public ExportDTO exportDocument(final ServiceUser user,
                                    final String uuid) {
        return database.transactionResult(configuration -> {

            final Record record = DSL.using(configuration)
                    .select()
                    .from(table)
                    .where(DocRefJooqEntity.UUID_FIELD.equal(uuid))
                    .fetchOne();

            return Optional.ofNullable(record)
                    .map(this::convertRecord)
                    .map(e -> {
                        final ExportDTO.Builder builder = new ExportDTO.Builder()
                                .value(DocRefEntity.NAME, e.getName());
                        valueExporter.exportValues(e, new ExportValue() {
                            @Override
                            public <T> void setValue(Field<T> field, T fieldValue) {
                                builder.value(field.getName(),
                                        Optional.ofNullable(fieldValue).
                                                map(Object::toString)
                                                .orElse(null));
                            }
                        });

                        return builder.build();
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
                                                   final Map<String, String> dataMap) {

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
