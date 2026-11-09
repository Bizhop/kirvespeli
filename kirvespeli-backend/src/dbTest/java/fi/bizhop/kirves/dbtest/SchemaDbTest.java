package fi.bizhop.kirves.dbtest;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaDbTest extends DbTestBase {
    // constraints enforced by the schema itself; entity validation (@NotNull) normally rejects these earlier

    @Test
    void gameAdminIsNotNull() {
        assertThrows(DataIntegrityViolationException.class,
                () -> this.jdbc.update("INSERT INTO games (admin_user_id) VALUES (NULL)"));
    }

    @Test
    void actionLogOwnerMustExist() {
        assertThrows(DataIntegrityViolationException.class,
                () -> this.jdbc.update("INSERT INTO action_logs (key, initial_state, owner_user_id) VALUES ('1-0', '{}', -1)"));
    }

    @Test
    void actionLogItemMustReferenceExistingLog() {
        var userId = this.jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Integer.class, SEEDED_USER_EMAIL);
        assertThrows(DataIntegrityViolationException.class,
                () -> this.jdbc.update("INSERT INTO action_log_items (user_id, action_log_id, input) VALUES (?, 'missing', '{}')", userId));
    }

    @Test
    void createdAtDefaultsToNow() {
        this.jdbc.update("INSERT INTO users (email) VALUES ('sql@example.com')");
        assertTrue(this.jdbc.queryForObject(
                "SELECT created_at > now() - interval '1 minute' FROM users WHERE email = 'sql@example.com'", Boolean.class));
    }

    @Test
    void liquibaseChangelogIsFullyApplied() {
        var applied = this.jdbc.queryForList("SELECT id FROM databasechangelog ORDER BY orderexecuted", String.class);
        assertEquals("CreateUserTable", applied.getFirst());
        assertEquals("removeMoneyFromUser", applied.getLast());
    }

    @Test
    void seededUserExists() {
        assertTrue(this.userRepo.findByEmail(SEEDED_USER_EMAIL).isPresent());
    }

    @Test
    void columnTypesArePostgresSpecific() {
        // TEXT columns have no length limit; an H2/Hibernate-generated schema would use VARCHAR(255)
        assertEquals("text", columnType("games", "game_data"));
        assertEquals("text", columnType("action_logs", "initial_state"));
        assertEquals("text", columnType("action_log_items", "input"));
        // SERIAL ids are 32-bit even though the entities map them to Long
        assertEquals("integer", columnType("users", "id"));
        assertEquals("timestamp without time zone", columnType("games", "created_at"));
    }

    private String columnType(String table, String column) {
        return this.jdbc.queryForObject(
                "SELECT data_type FROM information_schema.columns WHERE table_name = ? AND column_name = ?",
                String.class, table, column);
    }
}
