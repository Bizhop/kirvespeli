package fi.bizhop.jassu.model;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;
import fi.bizhop.jassu.util.RandomUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static fi.bizhop.jassu.model.Card.Rank.*;
import static fi.bizhop.jassu.model.Card.Suit.*;
import static fi.bizhop.jassu.model.KirvesGame.Action.*;
import static org.junit.Assert.*;

public class KirvesTest {
    static final List<User> TEST_USERS;

    static final List<Card> JACKS_AND_JOKERS = new ArrayList<>();
    static final List<Card> OTHER_CARDS = new ArrayList<>();

    static {
        TEST_USERS = List.of(
                new User("test1@example.com", ""),
                new User("test2@example.com", ""),
                new User("test3@example.com", ""),
                new User("test4@example.com", ""));

        try {
            JACKS_AND_JOKERS.addAll(List.of(
                    new Card(HEARTS, JACK),
                    new Card(HEARTS, JACK),
                    new Card(HEARTS, JACK),
                    new Card(HEARTS, JACK),
                    new Card(JOKER, BLACK),
                    new Card(JOKER, RED)));
        } catch (CardException e) {
            System.out.println("Unable to initialize card list");
            System.exit(1);
        }

        try {
            for(Card.Suit suit : Card.Suit.values()) {
                if(suit != JOKER) {
                    for(Card.Rank rank : Card.Rank.values()) {
                        if(!List.of(JACK, BLACK, RED).contains(rank)) {
                            OTHER_CARDS.add(new Card(suit, rank));
                        }
                    }
                }
            }
        } catch (CardException e) {
            System.out.println("Unable to initialize card list");
            System.exit(1);
        }
    }

    @Test
    public void testKirvesDeck() throws CardException {
        Cards kirvesDeck = new KirvesDeck();

        assertEquals(54, kirvesDeck.size());
    }

    @Test
    public void testAddingPlayers() throws CardException, KirvesGameException {
        KirvesGame game = getTestGame(TEST_USERS);

        assertEquals(4, game.out(TEST_USERS.get(0)).getPlayers().size());

        //adding same player should produce exception, but shouldn't increase number of players
        try {
            game.addPlayer(TEST_USERS.get(3));
        } catch (KirvesGameException e) {
            assertEquals("Player test4@example.com already joined game id=0", e.getMessage());
        }
        assertEquals(4, game.out(TEST_USERS.get(0)).getPlayers().size());
    }

    @Test
    public void testTurnOrderAndAvailableActions() throws CardException, KirvesGameException {
        KirvesGame game = getTestGame(TEST_USERS);

        User cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, JACKS_AND_JOKERS.get(RandomUtil.getInt(JACKS_AND_JOKERS.size())));
        assertNotNull(game.getCutCard());

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), DEAL));
        game.deal(TEST_USERS.get(0));
        assertNull(game.getCutCard());

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), DISCARD));
        game.discard(TEST_USERS.get(0), 0);
        assertEquals(0, game.out(null).getNumOfPlayedRounds());

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(3), DISCARD));
        game.discard(TEST_USERS.get(3), 0);
        assertEquals(0, game.out(null).getNumOfPlayedRounds());

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), PLAY_CARD));
        game.playCard(TEST_USERS.get(1), 0);
        assertEquals(0, game.out(null).getNumOfPlayedRounds());

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(2), PLAY_CARD));
        game.playCard(TEST_USERS.get(2), 0);
        assertEquals(0, game.out(null).getNumOfPlayedRounds());

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(3), PLAY_CARD));
        game.playCard(TEST_USERS.get(3), 0);
        assertEquals(0, game.out(null).getNumOfPlayedRounds());

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), PLAY_CARD));
        game.playCard(TEST_USERS.get(0), 0);
        assertEquals(1, game.out(null).getNumOfPlayedRounds());

        KirvesGameOut out = game.out(null);

        System.out.printf("Valtti: %s%n", out.getValtti());
        System.out.println(getRoundCards(out, 1, 0));

        KirvesPlayer winner = game.getRoundWinner(0).orElseThrow(KirvesGameException::new);
        System.out.printf("Round winner is %s%n", winner.getUserEmail());
        assertTrue(game.userHasActionAvailable(winner.getUser(), PLAY_CARD));
    }

    @Test
    public void testPlayingThroughFourHands() throws CardException, KirvesGameException {
        KirvesGame game = getTestGame(TEST_USERS);

        for (User dealer : TEST_USERS) {
            User cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
            game.cut(cutter, OTHER_CARDS.get(RandomUtil.getInt(OTHER_CARDS.size())));
            assertNotNull(game.getCutCard());

            assertTrue(game.userHasActionAvailable(dealer, DEAL));
            game.deal(dealer);
            playThroughHand(game, 5);
        }
    }

    @Test
    public void testWinningCards() throws CardException {
        //samaa maata, isompi voittaa
        List<Card> cards = List.of(new Card(SPADES, SEVEN), new Card(SPADES, TEN));
        assertEquals(1, KirvesGame.winningCard(cards, DIAMONDS));

        //eri maata, ajokortti voittaa
        cards = List.of(new Card(SPADES, SEVEN), new Card(CLUBS, TEN));
        assertEquals(0, KirvesGame.winningCard(cards, DIAMONDS));

        //valtti voittaa, vaikkaa on pienempi
        cards = List.of(new Card(SPADES, SEVEN), new Card(CLUBS, TWO));
        assertEquals(1, KirvesGame.winningCard(cards, CLUBS));

        //pamppu voittaa valttiässän
        cards = List.of(new Card(SPADES, ACE), new Card(SPADES, JACK));
        assertEquals(1, KirvesGame.winningCard(cards, SPADES));

        //pamppu voittaa hantin (lasketaan valtiksi)
        cards = List.of(new Card(CLUBS, ACE), new Card(SPADES, JACK));
        assertEquals(1, KirvesGame.winningCard(cards, HEARTS));

        //punainen jokeri voittaa mustan
        cards = List.of(new Card(JOKER, BLACK), new Card(JOKER, RED));
        assertEquals(1, KirvesGame.winningCard(cards, SPADES));

        //jokeri voittaa pampun
        cards = cards = List.of(new Card(CLUBS, JACK), new Card(JOKER, BLACK));
        assertEquals(1, KirvesGame.winningCard(cards, SPADES));
    }

    private void playThroughHand(KirvesGame game, int cardsInHand) throws CardException, KirvesGameException {
        for(int i=0; i < cardsInHand; i++) {
            playRound(game, false);
        }
    }

    private void playRound(KirvesGame game, boolean printMessage) throws KirvesGameException, CardException {
        User turn = TEST_USERS.stream()
                .filter(user -> game.userHasActionAvailable(user, PLAY_CARD))
                .findFirst()
                .orElseThrow(KirvesGameException::new);
        int index = TEST_USERS.indexOf(turn);
        List<User> nextRoundUsers = new ArrayList<>(TEST_USERS.subList(index, TEST_USERS.size()));
        if(index > 0) {
            nextRoundUsers.addAll(TEST_USERS.subList(0, index));
        }

        for (User player : nextRoundUsers) {
            if (!game.hasPlayer(player)) {
                throw new KirvesGameException(String.format("TEST: user %s is not in this game", player.getEmail()));
            }
            if(!game.userHasActionAvailable(player, PLAY_CARD)) {
                throw new KirvesGameException(String.format("TEST: user %s is not in turn", player.getEmail()));
            }
            game.playCard(player, 0);
        }
        if(printMessage) {
            System.out.println(game.getMessage());
        }
    }

    private KirvesGame getTestGame(List<User> players) throws CardException, KirvesGameException {
        if(players == null || players.isEmpty()) {
            throw new KirvesGameException("TEST: you must define at least one player");
        }
        KirvesGame game = new KirvesGame(players.get(0), 0L);
        if(players.size() > 1) {
            for(int i = 1; i < players.size(); i++) {
                game.addPlayer(players.get(i));
            }
        }
        return game;
    }

    private String getRoundCards(KirvesGameOut out, int offset, int round) {
        List<KirvesPlayerOut> players = out.getPlayers();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < players.size(); i++) {
            KirvesPlayerOut player = players.get((i + offset) % players.size());
            sb.append(player.getEmail());
            sb.append(": ");
            sb.append(player.getPlayedCards().get(round));
            if(player.getRoundsWon().contains(round)) {
                sb.append(" X");
            }
            if(i < players.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
