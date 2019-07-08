package stroom.query.testing.hibernate.app;

import stroom.datasource.api.v2.AbstractField;
import stroom.datasource.api.v2.TextField;
import stroom.query.audit.model.IsDataSourceField;
import stroom.query.hibernate.QueryableHibernateEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.function.Supplier;

@Entity(name="test_hibernate_entity")
public class TestQueryableHibernateEntity extends QueryableHibernateEntity {
    public static final String FLAVOUR = "flavour";
    public static final String ID = "id";

    private String id;

    private String flavour;

    public static class FlavourField implements Supplier<AbstractField> {
        @Override
        public AbstractField get() {
            return new TextField(FLAVOUR);
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

    public static class IdField implements Supplier<AbstractField> {
        @Override
        public AbstractField get() {
            return new stroom.datasource.api.v2.IdField(ID);
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
