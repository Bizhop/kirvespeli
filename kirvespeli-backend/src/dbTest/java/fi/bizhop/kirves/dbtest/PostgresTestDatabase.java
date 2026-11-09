package fi.bizhop.kirves.dbtest;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Creates a throwaway database on the configured PostgreSQL server once per test run and drops it
 * when the run ends. The server is the docker compose {@code db} service by default.
 */
public class PostgresTestDatabase implements BeforeAllCallback {
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/kirves";
    private static final String DEFAULT_CREDENTIAL = "kirves";
    private static final Pattern URL_PATTERN = Pattern.compile("^(jdbc:postgresql://[^/]+/)([^?]*)(.*)$");

    private static volatile Database database;

    @Override
    public void beforeAll(ExtensionContext context) {
        // root store values are closed when the whole test run ends, after Spring contexts are closed
        context.getRoot()
                .getStore(ExtensionContext.Namespace.create(PostgresTestDatabase.class))
                .computeIfAbsent(Database.class, key -> create(), Database.class);
    }

    public static String url() {
        return requireDatabase().url;
    }

    public static String username() {
        return setting("KIRVES_DBTEST_USERNAME", DEFAULT_CREDENTIAL);
    }

    public static String password() {
        return setting("KIRVES_DBTEST_PASSWORD", DEFAULT_CREDENTIAL);
    }

    private static Database requireDatabase() {
        if (database == null) throw new IllegalStateException("Test database not created, extend DbTestBase");
        return database;
    }

    private static synchronized Database create() {
        var serverUrl = setting("KIRVES_DBTEST_URL", DEFAULT_URL);
        var matcher = URL_PATTERN.matcher(serverUrl);
        if (!matcher.matches()) throw new IllegalStateException("Unsupported KIRVES_DBTEST_URL: " + serverUrl);

        var name = "kirves_test_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        try (var connection = connect(serverUrl); var statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE " + name);
        } catch (SQLException e) {
            throw new IllegalStateException(String.format(
                    "Cannot create test database on %s as %s: %s%n"
                            + "Start the database first: docker compose up -d db (in kirvespeli-backend), "
                            + "or set KIRVES_DBTEST_URL / KIRVES_DBTEST_USERNAME / KIRVES_DBTEST_PASSWORD.",
                    serverUrl, username(), e.getMessage()), e);
        }
        database = new Database(serverUrl, name, matcher.group(1) + name + matcher.group(3));
        return database;
    }

    private static Connection connect(String url) throws SQLException {
        return DriverManager.getConnection(url, username(), password());
    }

    private static String setting(String name, String defaultValue) {
        var value = System.getProperty(name);
        if (value == null || value.isBlank()) value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private record Database(String serverUrl, String name, String url) implements AutoCloseable {
        @Override
        public void close() throws SQLException {
            try (var connection = connect(this.serverUrl); var statement = connection.createStatement()) {
                // FORCE terminates connections still open, e.g. a pool that was not closed
                statement.execute("DROP DATABASE IF EXISTS " + this.name + " WITH (FORCE)");
            }
            database = null;
        }
    }
}
