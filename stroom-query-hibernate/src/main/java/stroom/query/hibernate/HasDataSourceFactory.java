package stroom.query.hibernate;

import io.dropwizard.db.DataSourceFactory;

public interface HasDataSourceFactory {
    DataSourceFactory getDataSourceFactory();
}
