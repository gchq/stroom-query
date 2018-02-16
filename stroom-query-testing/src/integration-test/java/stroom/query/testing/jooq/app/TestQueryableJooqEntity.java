package stroom.query.testing.jooq.app;

import stroom.datasource.api.v2.DataSourceField;
import stroom.query.api.v2.ExpressionTerm;
import stroom.query.audit.model.IsDataSourceField;
import stroom.query.jooq.QueryableJooqEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.function.Supplier;

@Entity(name="test_jooq_entity")
public class TestQueryableJooqEntity extends QueryableJooqEntity {
    public static final String COLOUR = "colour";
    public static final String ID = "id";

    private String id;

    private String colour;

    public static class ColourField implements Supplier<DataSourceField> {

        @Override
        public DataSourceField get() {
            return new DataSourceField(
                    DataSourceField.DataSourceFieldType.FIELD,
                    COLOUR,
                    true,
                    Arrays.asList(
                            ExpressionTerm.Condition.EQUALS,
                            ExpressionTerm.Condition.IN,
                            ExpressionTerm.Condition.IN_DICTIONARY
                    )
            );
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
