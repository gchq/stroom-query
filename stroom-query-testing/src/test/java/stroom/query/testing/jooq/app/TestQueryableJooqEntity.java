package stroom.query.testing.jooq.app;

import org.jooq.Field;
import org.jooq.impl.DSL;
import stroom.datasource.api.v2.AbstractField;
import stroom.datasource.api.v2.TextField;
import stroom.query.audit.model.IsDataSourceField;
import stroom.query.jooq.JooqEntity;
import stroom.query.jooq.QueryableJooqEntity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.function.Supplier;

@JooqEntity(tableName = "test_jooq_entity")
public class TestQueryableJooqEntity extends QueryableJooqEntity {
    public static final String COLOUR = "colour";
    public static final String ID = "id";

    public static final Field<String> ID_FIELD = DSL.field(ID, String.class);
    public static final Field<String> COLOUR_FIELD = DSL.field(COLOUR, String.class);

    private String id;

    private String colour;

    public static class ColourField implements Supplier<AbstractField> {
        @Override
        public AbstractField get() {
            return new TextField(COLOUR);
        }
    }

    @Id
    @Column(name = COLOUR)
    @IsDataSourceField(fieldSupplier = ColourField.class)
    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public static class IdField implements Supplier<AbstractField> {
        @Override
        public AbstractField get() {
            return new stroom.datasource.api.v2.IdField(ID);
        }
    }

    @Id
    @Column(name = ID)
    @IsDataSourceField(fieldSupplier = IdField.class)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
