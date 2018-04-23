package stroom.query.testing.jooq.app;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import stroom.security.ServiceUser;
import stroom.query.jooq.DocRefJooqEntity;
import stroom.query.jooq.JooqEntity;
import stroom.query.jooq.QueryableJooqEntity;
import stroom.query.testing.data.CreateTestDataResource;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

public class CreateTestDataJooqImpl implements CreateTestDataResource {

    private final DSLContext database;
    private final Table<Record> table;

    public static final int RECORDS_TO_CREATE = 1000;

    @Inject
    public CreateTestDataJooqImpl(final DSLContext database) {
        this.database = database;
        this.table = Optional.ofNullable(TestQueryableJooqEntity.class.getAnnotation(JooqEntity.class))
                .map(JooqEntity::tableName)
                .map(DSL::table)
                .orElseThrow(() -> new IllegalArgumentException("The TestQueryableHibernateEntity Class must be annotated with JooqEntity"));
    }

    @Override
    public Response createTestData(final ServiceUser user,
                                   final String docRefUuid,
                                   final String seed) {

        final ULong now = ULong.valueOf(System.currentTimeMillis());

        database.transaction(configuration -> {
            // Clear any existing data
            DSL.using(configuration)
                    .deleteFrom(this.table)
                    .execute();

            // Plonk a large number of rows in
            IntStream.range(0, RECORDS_TO_CREATE).forEach(x ->
                DSL.using(configuration)
                        .insertInto(this.table)
                        .columns(QueryableJooqEntity.DATA_SOURCE_UUID_FIELD,
                                DocRefJooqEntity.CREATE_USER_FIELD,
                                DocRefJooqEntity.CREATE_TIME_FIELD,
                                DocRefJooqEntity.UPDATE_USER_FIELD,
                                DocRefJooqEntity.UPDATE_TIME_FIELD,
                                TestQueryableJooqEntity.ID_FIELD,
                                TestQueryableJooqEntity.COLOUR_FIELD)
                        .values(docRefUuid,
                                user.getName(),
                                now,
                                user.getName(),
                                now,
                                UUID.randomUUID().toString(),
                                String.format("%s-%s", seed, UUID.randomUUID().toString()))
                        .execute()
            );
        });

        return Response.noContent().build();
    }
}
