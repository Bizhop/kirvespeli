package fi.bizhop.kirves.dbtest;

import fi.bizhop.kirves.db.UserDB;
import fi.bizhop.kirves.db.UserRepo;
import fi.bizhop.kirves.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA slice against a real PostgreSQL database, schema created by the application's Liquibase changelog.
 * Tests run without a wrapping test transaction, like the application does, so constraint violations
 * surface where production code sees them. Tables are emptied before each test.
 */
@ExtendWith(PostgresTestDatabase.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class DbTestBase {
    public static final String SEEDED_USER_EMAIL = "test@example.com";

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected UserRepo userRepo;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", PostgresTestDatabase::url);
        registry.add("spring.datasource.username", PostgresTestDatabase::username);
        registry.add("spring.datasource.password", PostgresTestDatabase::password);
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @BeforeEach
    void emptyTables() {
        this.jdbc.execute("TRUNCATE action_log_items, action_logs, games");
        this.jdbc.update("DELETE FROM users WHERE email <> ?", SEEDED_USER_EMAIL);
        this.jdbc.update("INSERT INTO users (email, nickname) VALUES (?, ?) ON CONFLICT (email) DO NOTHING",
                SEEDED_USER_EMAIL, "Testikäyttäjä");
    }

    protected UserDB saveUser(String email, String nickname) {
        var user = new UserDB(new User(email, null));
        user.nickname = nickname;
        return this.userRepo.save(user);
    }
}
