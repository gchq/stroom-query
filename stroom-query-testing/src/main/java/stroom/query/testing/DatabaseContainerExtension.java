package stroom.query.testing;

import org.testcontainers.containers.MySQLContainer;

import java.util.List;

public class DatabaseContainerExtension {
    public static final int MYSQL_EXPOSED_PORT = 14450;

    public MySQLContainer dbContainer = new MySQLContainer();

    void beforeAll() {
        dbContainer.setPortBindings(List.of(String.format("%d:%d", MYSQL_EXPOSED_PORT, MySQLContainer.MYSQL_PORT)));
        dbContainer.start();
    }

    void afterAll() {
        dbContainer.stop();
    }

    public MySQLContainer getDbContainer() {
        return dbContainer;
    }
}
