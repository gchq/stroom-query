package stroom.query.testing.jooq.app;

import org.jooq.Field;
import stroom.datasource.api.v2.DataSourceField;
import stroom.query.api.v2.ExpressionTerm;
import stroom.query.audit.model.IsDataSourceField;
import stroom.query.jooq.JooqEntity;
import stroom.query.jooq.QueryableJooqEntity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.function.Supplier;

import static org.jooq.impl.DSL.field;

@JooqEntity(tableName="test_jooq_entity")
public class TestQueryableJooqEntity extends QueryableJooqEntity {
    public static final String COLOUR = "colour";
    public static final String ID = "id";

    public static final Field<String> ID_FIELD = field(ID, String.class);
    public static final Field<String> COLOUR_FIELD = field(COLOUR, String.class);

    private String id;

    private String colour;

    public static class ColourField implements Supplier<DataSourceField> {

        @Override
        public DataSourceField get() {
            return new DataSourceField.Builder()
                    .type(DataSourceField.DataSourceFieldType.FIELD)
                    .name(COLOUR)
                    .queryable(true)
                    .addConditions(
                            ExpressionTerm.Condition.EQUALS,
                            ExpressionTerm.Condition.IN,
                            ExpressionTerm.Condition.IN_DICTIONARY
                    ).build();
        }
    }

    @Id
    @Column(name= COLOUR)
    @IsDataSourceField(fieldSupplier = ColourField.class)
    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public static class IdField implements Supplier<DataSourceField> {

        @Override
        public DataSourceField get() {
            return new DataSourceField.Builder()
                    .type(DataSourceField.DataSourceFieldType.ID)
                    .name(ID)
                    .queryable(true)
                    .addConditions(
                            ExpressionTerm.Condition.EQUALS,
                            ExpressionTerm.Condition.IN,
                            ExpressionTerm.Condition.IN_DICTIONARY
                    ).build();
        }
    }

    @Id
    @Column(name=ID)
    @IsDataSourceField(fieldSupplier = IdField.class)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
