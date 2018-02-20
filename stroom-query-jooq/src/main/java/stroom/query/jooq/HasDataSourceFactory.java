package stroom.query.jooq;

import io.dropwizard.db.DataSourceFactory;

public interface HasDataSourceFactory {
    DataSourceFactory getDataSourceFactory();
}
