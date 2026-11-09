package fi.bizhop.kirves.dbtest;

import fi.bizhop.kirves.db.KirvesGameRepo;
import fi.bizhop.kirves.exception.CardException;
import fi.bizhop.kirves.exception.KirvesGameException;
import fi.bizhop.kirves.model.Card;
import fi.bizhop.kirves.model.User;
import fi.bizhop.kirves.model.kirves.Game;
import fi.bizhop.kirves.model.kirves.Game.Action;
import fi.bizhop.kirves.model.kirves.in.GameIn;
import fi.bizhop.kirves.service.KirvesService;
import fi.bizhop.kirves.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;

import static fi.bizhop.kirves.model.kirves.Game.Action.*;
import static org.junit.jupiter.api.Assertions.*;

/** Game flow through the service, persisted to PostgreSQL. */
@Import({KirvesService.class, UserService.class})
class KirvesServiceDbTest extends DbTestBase {
    private static final List<Action> ACTION_PRIORITY =
            List.of(CUT, DEAL, ACE_OR_TWO_DECISION, SPEAK, SPEAK_SUIT, DISCARD, PLAY_CARD);

    @Autowired
    KirvesService kirvesService;

    @Autowired
    KirvesGameRepo gameRepo;

    private final User admin = new User("admin@example.com", null);
    private final User player = new User("player@example.com", null);

    @Test
    void initJoinAndInactivate() throws Exception {
        saveUser(this.admin.getEmail(), "Admin");
        saveUser(this.player.getEmail(), "Player");

        var id = this.kirvesService.init(this.admin);
        this.kirvesService.joinGame(id, this.player);

        var db = this.gameRepo.findById(id).orElseThrow();
        assertEquals(2, db.players);
        assertTrue(db.gameData.contains(this.player.getEmail()));
        assertEquals(1, this.kirvesService.getActiveGames(this.player).size());

        this.kirvesService.inactivateGame(id, this.admin);
        assertFalse(this.gameRepo.findById(id).orElseThrow().active);
        assertTrue(this.kirvesService.getActiveGames(this.player).isEmpty());
    }

    @Test
    void actionLogIsPersistedAndRestoreTruncatesIt() throws Exception {
        saveUser(this.admin.getEmail(), "Admin");
        saveUser(this.player.getEmail(), "Player");
        var id = this.kirvesService.init(this.admin);
        this.kirvesService.joinGame(id, this.player);

        // play until the hand's action log has a few items (DEAL plus at least two more)
        Game game = this.kirvesService.getGame(id);
        for (int step = 0; step < 30 && logItemCount() < 3; step++) {
            game = playNext(id, game);
        }
        assertTrue(logItemCount() >= 3, "expected the game to progress");

        var handId = game.getCurrentHandId();
        var key = id + "-" + handId;
        assertEquals(1, this.jdbc.queryForObject("SELECT count(*) FROM action_logs WHERE key = ?", Integer.class, key));
        assertEquals(logItemCount(), this.kirvesService.getActionLog(id, handId, this.admin).getItems().size());

        // restoreGame deletes with a derived delete query, which needs the method's transaction
        this.kirvesService.restoreGame(id, handId, 0, this.admin);

        assertEquals(1, logItemCount());
        assertEquals(1, this.kirvesService.getActionLog(id, handId, this.admin).getItems().size());
    }

    private int logItemCount() {
        return this.jdbc.queryForObject("SELECT count(*) FROM action_log_items", Integer.class);
    }

    private Game playNext(Long id, Game game) throws Exception {
        for (var action : ACTION_PRIORITY) {
            for (var user : List.of(this.admin, this.player)) {
                if (!game.userHasActionAvailable(user, action)) continue;
                if (action == PLAY_CARD || action == DISCARD) {
                    for (int index = 0; index < 6; index++) {
                        try {
                            return this.kirvesService.action(id, GameIn.builder().action(action).index(index).build(), user);
                        } catch (CardException | KirvesGameException e) {
                            // not a valid card for this play, try the next one
                        }
                    }
                    fail("No playable card for " + user.getEmail());
                }
                var in = switch (action) {
                    case CUT -> GameIn.builder().action(CUT).declineCut(false);
                    case ACE_OR_TWO_DECISION -> GameIn.builder().action(action).keepExtraCard(true);
                    case SPEAK -> GameIn.builder().action(action).speak(Game.Speak.KEEP);
                    case SPEAK_SUIT -> GameIn.builder().action(action).suit(Card.Suit.HEARTS);
                    default -> GameIn.builder().action(action);
                };
                return this.kirvesService.action(id, in.build(), user);
            }
        }
        throw new AssertionError("No available actions");
    }
}
