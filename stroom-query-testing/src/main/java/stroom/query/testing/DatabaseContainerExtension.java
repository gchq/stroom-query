package stroom.query.testing;

import org.testcontainers.containers.MySQLContainer;

import java.util.List;

public class DatabaseContainerExtension {
    public MySQLContainer dbContainer = new MySQLContainer();

    void beforeAll() {
        dbContainer.setPortBindings(List.of(String.format("4450:%d", MySQLContainer.MYSQL_PORT)));
        dbContainer.start();
    }

    void afterAll() {
        dbContainer.stop();
    }

    public MySQLContainer getDbContainer() {
        return dbContainer;
    }
}
