package fi.bizhop.kirves.dbtest;

import fi.bizhop.kirves.db.KirvesGameDB;
import fi.bizhop.kirves.db.KirvesGameRepo;
import fi.bizhop.kirves.db.UserDB;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameDbTest extends DbTestBase {
    @Autowired
    KirvesGameRepo gameRepo;

    @Test
    void activeGameQueries() {
        var admin = saveUser("admin@example.com", "Admin");
        var active = saveGame(admin, true, "{}");
        var inactive = saveGame(admin, false, "{}");

        var activeIds = this.gameRepo.findByActiveTrue().stream().map(game -> game.id).toList();
        assertEquals(List.of(active.id), activeIds);

        assertTrue(this.gameRepo.findByIdAndActiveTrue(active.id).isPresent());
        assertTrue(this.gameRepo.findByIdAndActiveTrue(inactive.id).isEmpty());
    }

    @Test
    void nullActiveIsRejected() {
        var admin = saveUser("admin@example.com", "Admin");
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                () -> saveGame(admin, null, "{}"));
    }

    @Test
    void largeGameDataRoundTrip() {
        var gameData = "{\"data\":\"" + "x".repeat(1_000_000) + "ä€\"}";
        var game = saveGame(saveUser("admin@example.com", "Admin"), true, gameData);

        var reloaded = this.gameRepo.findById(game.id).orElseThrow();
        assertEquals(gameData, reloaded.gameData);
    }

    @Test
    void adminIsLoadedWithGame() {
        var game = saveGame(saveUser("admin@example.com", "Admin"), true, "{}");

        var reloaded = this.gameRepo.findById(game.id).orElseThrow();
        assertEquals("admin@example.com", reloaded.admin.email);
        assertNotNull(reloaded.createdAt);
    }

    private KirvesGameDB saveGame(UserDB admin, Boolean active, String gameData) {
        var game = new KirvesGameDB();
        game.admin = admin;
        game.active = active;
        game.players = 1;
        game.canJoin = true;
        game.gameData = gameData;
        return this.gameRepo.save(game);
    }
}
