package fi.bizhop.kirves.model.kirves;

import fi.bizhop.kirves.exception.CardException;
import fi.bizhop.kirves.exception.KirvesGameException;
import fi.bizhop.kirves.model.Card;
import fi.bizhop.kirves.model.User;
import fi.bizhop.kirves.model.kirves.out.ScoreHistoryOut;
import fi.bizhop.kirves.model.kirves.out.ScoreOut;
import fi.bizhop.kirves.model.kirves.pojo.GameDataPOJO;
import fi.bizhop.kirves.util.JsonUtil;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.bizhop.kirves.model.Card.Rank.*;
import static fi.bizhop.kirves.model.Card.Suit.*;
import static fi.bizhop.kirves.model.kirves.Game.Action.*;
import static fi.bizhop.kirves.model.kirves.Game.Speak.KEEP;
import static fi.bizhop.kirves.util.TestUserUtil.getTestUser;
import static org.junit.jupiter.api.Assertions.*;

public class GameScoringTest {
    private static final User A = getTestUser("a@example.com");
    private static final User B = getTestUser("b@example.com");
    private static final User C = getTestUser("c@example.com");

    @Test
    public void testScoresInSeatingOrderWithDuplicateNicknames() throws Exception {
        var users = List.of(user("x@example.com", "Sama"), user("y@example.com", "Sama"), user("z@example.com", "Muu"));
        var game = newGame(users);

        var out = game.out(users.get(2));
        assertEquals(List.of(
                new ScoreOut("x@example.com", "Sama", 0),
                new ScoreOut("y@example.com", "Sama", 0),
                new ScoreOut("z@example.com", "Muu", 0)), out.getScores());
        //players output is rotated to the viewer, scores are not
        assertEquals("z@example.com", out.getPlayers().get(0).getEmail());
    }

    @Test
    public void testSimultaneousWinnersEndGame() throws Exception {
        var game = withScores(newGame(List.of(A, B, C)), 2, 2, 0);

        game.handleScoring(Set.of(player(game, A), player(game, B)));

        var out = game.out();
        assertEquals(List.of(new ScoreHistoryOut(List.of(
                new ScoreOut(A.getEmail(), A.getNickname(), 3),
                new ScoreOut(B.getEmail(), B.getNickname(), 3),
                new ScoreOut(C.getEmail(), C.getNickname(), 0)), C.getEmail(), false)), out.getScoresHistory());
        assertTrue(out.getScores().stream().allMatch(score -> score.score() == 0));
        assertEquals(3, out.getPlayers().stream().filter(p -> !p.isFolded()).count());
        assertTrue(game.out().getMessages().contains("Hävisi: " + C.getNickname()));
    }

    @Test
    public void testFoldEndsGameAndScoresSurviveJsonRoundTrip() throws Exception {
        var game = withScores(newGame(List.of(A, B)), 0, 2);
        game.cut(B, false, new Card(SPADES, SEVEN), null);
        game.deal(A, safeCards());
        game.speak(B, KEEP);
        assertTrue(game.userHasActionAvailable(A, FOLD));

        game.fold(A);

        game = roundTrip(game);
        var history = game.out().getScoresHistory();
        assertEquals(1, history.size());
        assertEquals(A.getEmail(), history.get(0).loser());
        assertEquals(List.of(0, 3), history.get(0).scores().stream().map(ScoreOut::score).toList());
        assertEquals(List.of(0, 0), game.out().getScores().stream().map(ScoreOut::score).toList());
    }

    @Test
    public void testStickThenAxeThenNewMatch() throws Exception {
        var game = newGame(List.of(A, B, C));

        game = loseGame(game, C);
        game = loseGame(game, A);
        game = loseGame(game, C);  //C's second loss: axe, match ends
        game = loseGame(game, C);  //new match: stick
        game = loseGame(game, A);  //A's earlier loss was in the previous match: stick
        game = loseGame(game, A);  //axe

        var history = game.out().getScoresHistory();
        assertEquals(List.of(C, A, C, C, A, A).stream().map(User::getEmail).toList(),
                history.stream().map(ScoreHistoryOut::loser).toList());
        assertEquals(List.of(false, false, true, false, false, true),
                history.stream().map(ScoreHistoryOut::axe).toList());
        assertTrue(game.out().getMessages().contains(C.getNickname() + " sai kirveen"));
    }

    @Test
    public void testLoserDealsAndCanChooseDealerUntilCut() throws Exception {
        var game = loseGame(newGame(List.of(A, B, C)), B);

        assertEquals(B.getEmail(), game.out().getDealer());
        assertTrue(game.userHasActionAvailable(B, SET_DEALER));
        assertFalse(game.userHasActionAvailable(A, SET_DEALER));
        assertTrue(game.userHasActionAvailable(A, CUT));

        game.chooseDealer(B, C.getEmail());
        assertEquals(C.getEmail(), game.out().getDealer());
        assertTrue(game.userHasActionAvailable(B, CUT));
        assertTrue(game.userHasActionAvailable(B, SET_DEALER));
        assertTrue(game.out().getMessages().contains(
                String.format("%s valitsi jakajaksi pelaajan %s", B.getNickname(), C.getNickname())));

        game = roundTrip(game);
        assertTrue(game.userHasActionAvailable(B, SET_DEALER));

        game.cut(B, false, new Card(SPADES, SEVEN), null);
        assertFalse(game.userHasActionAvailable(B, SET_DEALER));
        assertTrue(game.userHasActionAvailable(C, DEAL));
    }

    @Test
    public void testChooseUnknownDealer() throws Exception {
        var game = loseGame(newGame(List.of(A, B)), A);

        assertThrows(KirvesGameException.class, () -> game.chooseDealer(A, "nobody@example.com"));
        assertEquals(A.getEmail(), game.out().getDealer());
    }

    //every other player wins the hand at 2 points, leaving the loser below 3
    private static Game loseGame(Game game, User loser) throws KirvesGameException {
        var winners = game.out().getScores().stream()
                .filter(score -> !score.email().equals(loser.getEmail()))
                .map(ScoreOut::email)
                .collect(Collectors.toSet());
        var pojo = JsonUtil.getJavaObject(game.toJson(), GameDataPOJO.class).orElseThrow();
        pojo.scores.stream().filter(score -> winners.contains(score.email)).forEach(score -> score.score = 2);
        var restored = new Game(pojo);
        var winnerPlayers = winners.stream()
                .map(email -> restored.getPlayer(email).orElseThrow())
                .collect(Collectors.toSet());
        restored.handleScoring(winnerPlayers);
        return restored;
    }

    private static Game withScores(Game game, int... scores) throws KirvesGameException {
        var pojo = JsonUtil.getJavaObject(game.toJson(), GameDataPOJO.class).orElseThrow();
        for(int i = 0; i < scores.length; i++) {
            pojo.scores.get(i).score = scores[i];
        }
        return new Game(pojo);
    }

    private static Game roundTrip(Game game) throws KirvesGameException {
        return new Game(JsonUtil.getJavaObject(game.toJson(), GameDataPOJO.class).orElseThrow());
    }

    private static Game newGame(List<User> users) throws CardException, KirvesGameException {
        var game = new Game(users.get(0));
        for(var user : users.subList(1, users.size())) {
            game.addPlayer(user);
        }
        return game;
    }

    private static Player player(Game game, User user) {
        return game.getPlayer(user.getEmail()).orElseThrow();
    }

    private static User user(String email, String nickname) {
        var user = new User(email, "");
        user.setNickname(nickname);
        return user;
    }

    private static List<Card> safeCards() throws CardException {
        var cards = new ArrayList<Card>();
        for(var suit : List.of(SPADES, HEARTS, CLUBS, DIAMONDS)) {
            for(var rank : List.of(THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, QUEEN, KING)) {
                cards.add(new Card(suit, rank));
            }
        }
        return cards;
    }
}
