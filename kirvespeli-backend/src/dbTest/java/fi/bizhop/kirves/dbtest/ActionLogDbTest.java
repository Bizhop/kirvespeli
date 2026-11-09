package fi.bizhop.kirves.dbtest;

import fi.bizhop.kirves.db.ActionLogDB;
import fi.bizhop.kirves.db.ActionLogItemDB;
import fi.bizhop.kirves.db.ActionLogItemRepo;
import fi.bizhop.kirves.db.ActionLogRepo;
import fi.bizhop.kirves.db.UserDB;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;

class ActionLogDbTest extends DbTestBase {
    @Autowired
    ActionLogRepo actionLogRepo;

    @Autowired
    ActionLogItemRepo actionLogItemRepo;

    @Autowired
    TransactionTemplate transactionTemplate;

    @Test
    void keyColumnWorks() {
        // "key" is a reserved word in H2, a non-reserved keyword in PostgreSQL
        var owner = saveUser("owner@example.com", null);
        saveLog("12-3", owner);

        var reloaded = this.actionLogRepo.findById("12-3").orElseThrow();
        assertEquals("{\"initial\":true}", reloaded.initialState);
        assertEquals("owner@example.com", reloaded.owner.email);
    }

    @Test
    void itemsAreLoadedWithLog() {
        var owner = saveUser("owner@example.com", null);
        var log = saveLog("1-0", owner);
        saveItem(log, owner, "{\"action\":\"DEAL\"}");
        saveItem(log, owner, "{\"action\":\"FOLD\"}");

        var items = this.transactionTemplate.execute(status ->
                this.actionLogRepo.findById("1-0").orElseThrow().items.stream().map(item -> item.input).toList());
        assertEquals(2, items.size());
    }

    @Test
    void deleteByActionLogInTransaction() {
        var owner = saveUser("owner@example.com", null);
        var log = saveLog("1-0", owner);
        var other = saveLog("2-0", owner);
        saveItem(log, owner, "{}");
        saveItem(log, owner, "{}");
        saveItem(other, owner, "{}");

        var deleted = this.transactionTemplate.execute(status -> this.actionLogItemRepo.deleteByActionLog(log));

        assertEquals(2L, deleted);
        assertEquals(1, this.jdbc.queryForObject("SELECT count(*) FROM action_log_items", Integer.class));
    }

    private ActionLogDB saveLog(String key, UserDB owner) {
        var log = new ActionLogDB();
        log.key = key;
        log.owner = owner;
        log.initialState = "{\"initial\":true}";
        return this.actionLogRepo.save(log);
    }

    private void saveItem(ActionLogDB log, UserDB user, String input) {
        var item = new ActionLogItemDB();
        item.actionLog = log;
        item.user = user;
        item.input = input;
        this.actionLogItemRepo.save(item);
    }
}
