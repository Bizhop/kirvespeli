package fi.bizhop.kirves.model.kirves;

import fi.bizhop.kirves.exception.CardException;
import fi.bizhop.kirves.exception.KirvesGameException;
import fi.bizhop.kirves.model.User;
import fi.bizhop.kirves.model.kirves.pojo.GameDataPOJO;
import fi.bizhop.kirves.util.JsonUtil;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static fi.bizhop.kirves.exception.KirvesGameException.Type.BAD_REQUEST;
import static fi.bizhop.kirves.model.kirves.Game.Action.PLAY_CARD;
import static fi.bizhop.kirves.util.TestUserUtil.getTestUser;
import static org.junit.jupiter.api.Assertions.*;

class GameFollowSuitTest {
    private static final User A = getTestUser("a@example.com");
    private static final User B = getTestUser("b@example.com");

    @Test
    void rejectsOffSuitWithoutChangingState() throws Exception {
        var game = game(List.of("AS", "2H"), List.of("KS", "AH", "JS", "BJ"));
        game.playCard(A, 0);
        for(int index : List.of(1, 2, 3)) {
            var before = game.toJson();
            var error = assertThrows(KirvesGameException.class, () -> game.playCard(B, index));
            assertEquals(BAD_REQUEST, error.getType());
            assertEquals(before, game.toJson());
        }
        game.playCard(B, 0);
        assertEquals(1, game.out().getNumOfPlayedRounds());
        assertEquals("", game.out().getFirstCardSuit());
        game.playCard(A, 0);
        assertEquals("HEARTS", game.out().getFirstCardSuit());
    }

    @Test
    void jacksAndJokersMustBeFollowedAsTrumps() throws Exception {
        for(var lead : List.of("AH", "JS", "JC", "BJ", "RJ")) {
            for(var follow : List.of("KH", "JD", "JH", "BJ", "RJ")) {
                if(lead.equals(follow)) continue;
                var game = game(List.of(lead, "2C"), List.of("AS", follow));
                game.playCard(A, 0);
                assertEquals("HEARTS", game.out().getFirstCardSuit());
                assertThrows(KirvesGameException.class, () -> game.playCard(B, 0));
                game.playCard(B, 1);
                assertEquals(1, game.out().getNumOfPlayedRounds());
            }
        }
    }

    @Test
    void canDiscardOrTrumpWhenVoidIncludingPrintedSuitOfJack() throws Exception {
        for(var follow : List.of("AH", "JS", "BJ", "2D")) {
            var game = game(List.of("AS", "2C"), List.of(follow, "JD"));
            game.playCard(A, 0);
            game.playCard(B, 0);
            assertEquals(1, game.out().getNumOfPlayedRounds());
        }
        var game = game(List.of("RJ", "2C"), List.of("AS", "2D"));
        game.playCard(A, 0);
        game.playCard(B, 0);
        assertEquals(1, game.out().getNumOfPlayedRounds());
    }

    @Test
    void replayingAnOldOffSuitMoveDoesNotDisableLaterValidation() throws Exception {
        var game = game(List.of("AS", "AC", "2H"), List.of("KS", "2D", "KC", "AH"));
        game.playCard(A, 0);
        game.replayCard(B, 1);
        assertEquals(1, game.out().getNumOfPlayedRounds());
        game.playCard(A, 0);
        assertThrows(KirvesGameException.class, () -> game.playCard(B, 0));
        game.playCard(B, 1);
        assertEquals(2, game.out().getNumOfPlayedRounds());
    }

    @Test
    void invalidIndexesLeaveStateUnchanged() throws Exception {
        var game = game(List.of("AS", "2H"), List.of("KS", "AH"));
        game.playCard(A, 0);
        var before = game.toJson();
        assertThrows(CardException.class, () -> game.playCard(B, -1));
        assertThrows(CardException.class, () -> game.playCard(B, 2));
        assertEquals(before, game.toJson());
    }

    private static Game game(List<String> a, List<String> b) throws Exception {
        var game = new Game(A);
        game.addPlayer(B);
        var data = JsonUtil.getJavaObject(game.toJson(), GameDataPOJO.class).orElseThrow();
        data.turn = A.getEmail();
        data.firstPlayerOfRound = A.getEmail();
        data.trump = "H";
        data.players.get(0).hand = a;
        data.players.get(0).availableActions = new ArrayList<>(List.of(PLAY_CARD));
        data.players.get(1).hand = b;
        data.players.get(1).availableActions = new ArrayList<>();
        return new Game(data);
    }
}
