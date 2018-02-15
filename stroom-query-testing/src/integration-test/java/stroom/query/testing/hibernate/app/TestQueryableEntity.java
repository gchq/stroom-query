package stroom.query.testing.hibernate.app;

import stroom.datasource.api.v2.DataSourceField;
import stroom.query.api.v2.ExpressionTerm;
import stroom.query.audit.model.IsDataSourceField;
import stroom.query.hibernate.QueryableHibernateEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.function.Supplier;

@Entity(name="test_entity")
public class TestQueryableEntity extends QueryableHibernateEntity {
    public static final String FLAVOUR = "flavour";
    public static final String ID = "id";

    private String id;

    private String flavour;

    public static class FlavourField implements Supplier<DataSourceField> {

        @Override
        public DataSourceField get() {
            return new DataSourceField(
                    DataSourceField.DataSourceFieldType.FIELD,
                    FLAVOUR,
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
    @Column(name= FLAVOUR)
    @IsDataSourceField(fieldSupplier = FlavourField.class)
    public String getFlavour() {
        return flavour;
    }

    public void setFlavour(String flavour) {
        this.flavour = flavour;
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
