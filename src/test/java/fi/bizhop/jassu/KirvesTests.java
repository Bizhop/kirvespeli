package fi.bizhop.jassu;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;
import fi.bizhop.jassu.models.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static fi.bizhop.jassu.models.Card.Rank.*;
import static fi.bizhop.jassu.models.Card.Suit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KirvesTests {
    static final List<User> testUsers;

    static {
        List<User> temp = new ArrayList<>();
        temp.add(new User("test1@example.com", ""));
        temp.add(new User("test2@example.com", ""));
        temp.add(new User("test3@example.com", ""));
        temp.add(new User("test4@example.com", ""));
        testUsers = Collections.unmodifiableList(temp);
    }

    @Test
    public void testKirvesDeck() throws CardException {
        Cards kirvesDeck = new KirvesDeck();

        assertEquals(54, kirvesDeck.size());
    }

    @Test
    public void testAddingPlayers() throws CardException, KirvesGameException {
        KirvesGame game = getTestGame(testUsers);

        assertEquals(4, game.out(testUsers.get(0)).getPlayers().size());

        //adding same player should produce exception, but shouldn't increase number of players
        try {
            game.addPlayer(testUsers.get(3));
        } catch (KirvesGameException e) {
            assertEquals("Player test4@example.com already joined game id=0", e.getMessage());
        }
        assertEquals(4, game.out(testUsers.get(0)).getPlayers().size());
    }

    @Test
    public void testTurnOrder() throws CardException, KirvesGameException {
        KirvesGame game = getTestGame(testUsers);

        game.deal(testUsers.get(0));

        assertTrue(game.isMyTurn(testUsers.get(1)));
        game.playCard(testUsers.get(1), 0);

        assertTrue(game.isMyTurn(testUsers.get(2)));
        game.playCard(testUsers.get(2), 0);

        assertTrue(game.isMyTurn(testUsers.get(3)));
        game.playCard(testUsers.get(3), 0);

        assertTrue(game.isMyTurn(testUsers.get(0)));
        game.playCard(testUsers.get(0), 0);

        KirvesGameOut out = game.out(null);
        System.out.println(out.getValtti());
        System.out.println(getRoundCards(out, 1, 0));

        KirvesPlayer winner = game.getRoundWinner(0).orElseThrow(KirvesGameException::new);
        System.out.printf("Round winner is %s%n", winner.getUserEmail());
        assertTrue(game.isMyTurn(winner.getUser()));
    }

    @Test
    public void testPlayingThroughFourHands() throws CardException, KirvesGameException {
        KirvesGame game = getTestGame(testUsers);

        for (User dealer : testUsers) {
            assertTrue(game.userCanDeal(dealer));
            game.deal(dealer);
            playThroughHand(game, 5);
        }
    }

    @Test
    public void testWinningCards() throws CardException {
        //samaa maata, isompi voittaa
        List<Card> cards = Arrays.asList(new Card(SPADES, SEVEN), new Card(SPADES, TEN));
        assertEquals(1, KirvesGame.winningCard(cards, DIAMONDS));

        //eri maata, ajokortti voittaa
        cards = Arrays.asList(new Card(SPADES, SEVEN), new Card(CLUBS, TEN));
        assertEquals(0, KirvesGame.winningCard(cards, DIAMONDS));

        //valtti voittaa, vaikkaa on pienempi
        cards = Arrays.asList(new Card(SPADES, SEVEN), new Card(CLUBS, TWO));
        assertEquals(1, KirvesGame.winningCard(cards, CLUBS));

        //pamppu voittaa valttiässän
        cards = Arrays.asList(new Card(SPADES, ACE), new Card(SPADES, JACK));
        assertEquals(1, KirvesGame.winningCard(cards, SPADES));

        //pamppu voittaa hantin (lasketaan valtiksi)
        cards = Arrays.asList(new Card(CLUBS, ACE), new Card(SPADES, JACK));
        assertEquals(1, KirvesGame.winningCard(cards, HEARTS));

        //punainen jokeri voittaa mustan
        cards = Arrays.asList(new Card(JOKER, BLACK), new Card(JOKER, RED));
        assertEquals(1, KirvesGame.winningCard(cards, SPADES));

        //jokeri voittaa pampun
        cards = cards = Arrays.asList(new Card(CLUBS, JACK), new Card(JOKER, BLACK));
        assertEquals(1, KirvesGame.winningCard(cards, SPADES));
    }

    private void playThroughHand(KirvesGame game, int cardsInHand) throws CardException, KirvesGameException {
        for(int i=0; i < cardsInHand; i++) {
            playRound(game, true);
        }
    }

    private void playRound(KirvesGame game, boolean printMessage) throws KirvesGameException, CardException {
        User turn = testUsers.stream().filter(game::isMyTurn).findFirst().orElseThrow(KirvesGameException::new);
        int index = testUsers.indexOf(turn);
        List<User> nextRoundUsers = new ArrayList<>(testUsers.subList(index, testUsers.size()));
        if(index > 0) {
            nextRoundUsers.addAll(testUsers.subList(0, index));
        }

        for (User player : nextRoundUsers) {
            if (!game.hasPlayer(player)) {
                throw new KirvesGameException(String.format("TEST: user %s is not in this game", player.getEmail()));
            }
            if(!game.isMyTurn(player)) {
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
