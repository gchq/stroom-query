package stroom.query.testing;

import org.testcontainers.containers.MySQLContainer;

import java.util.List;

public class DatabaseContainerExtension {
    private static final int MYSQL_EXPOSED_PORT = 14450;

    private static MySQLContainer dbContainer = new MySQLContainer();

    void beforeAll() {
        if (!dbContainer.isRunning()) {
            dbContainer.setPortBindings(List.of(String.format("%d:%d", MYSQL_EXPOSED_PORT, MySQLContainer.MYSQL_PORT)));
            dbContainer.start();
        }
    }

    void afterAll() {
        // There doesn't seem to be a way to know if this is the 'last test', but the container seems to get cleaned up anyway...
        //dbContainer.stop();
    }

    public MySQLContainer getDbContainer() {
        return dbContainer;
    }
}
