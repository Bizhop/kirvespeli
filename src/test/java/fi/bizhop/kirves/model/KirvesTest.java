package fi.bizhop.kirves.model;

import fi.bizhop.kirves.exception.CardException;
import fi.bizhop.kirves.exception.KirvesGameException;
import fi.bizhop.kirves.model.kirves.*;
import fi.bizhop.kirves.model.kirves.out.GameOut;
import fi.bizhop.kirves.model.kirves.out.PlayerOut;
import fi.bizhop.kirves.model.kirves.pojo.GameDataPOJO;
import fi.bizhop.kirves.model.kirves.pojo.PlayerPOJO;
import fi.bizhop.kirves.model.kirves.pojo.UserPOJO;
import fi.bizhop.kirves.util.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.bizhop.kirves.model.Card.Rank.*;
import static fi.bizhop.kirves.model.Card.Suit.*;
import static fi.bizhop.kirves.model.kirves.Game.Action.*;
import static fi.bizhop.kirves.model.kirves.Game.Speak.*;
import static org.junit.jupiter.api.Assertions.*;

public class KirvesTest {
    static final List<User> TEST_USERS;

    static List<Card> JACKS_AND_JOKERS;
    static List<Card> OTHER_CARDS;
    static List<Card> TWOS_AND_ACES;

    static {
        TEST_USERS = List.of(
                new User("test0@example.com", ""),
                new User("test1@example.com", ""),
                new User("test2@example.com", ""),
                new User("test3@example.com", ""));

        try {
            JACKS_AND_JOKERS = List.of(
                    new Card(HEARTS, JACK),
                    new Card(SPADES, JACK),
                    new Card(CLUBS, JACK),
                    new Card(DIAMONDS, JACK),
                    new Card(JOKER, BLACK),
                    new Card(JOKER, RED));
        } catch (CardException e) {
            System.out.println("Unable to initialize card list");
            System.exit(1);
        }

        try {
            List<Card> temp = new ArrayList<>();
            for(Card.Suit suit : Card.Suit.values()) {
                if(suit != JOKER) {
                    for(Card.Rank rank : Card.Rank.values()) {
                        if(List.of(TWO, ACE).contains(rank)) {
                            temp.add(new Card(suit, rank));
                        }
                    }
                }
            }
            TWOS_AND_ACES = Collections.unmodifiableList(temp);
        } catch (CardException e) {
            System.out.println("Unable to initialize card list");
            System.exit(1);
        }

        try {
            List<Card> temp = new ArrayList<>();
            for(Card.Suit suit : Card.Suit.values()) {
                if(suit != JOKER) {
                    for(Card.Rank rank : Card.Rank.values()) {
                        if(!List.of(JACK, BLACK, RED, TWO, ACE).contains(rank)) {
                            temp.add(new Card(suit, rank));
                        }
                    }
                }
            }
            OTHER_CARDS = Collections.unmodifiableList(temp);
        } catch (CardException e) {
            System.out.println("Unable to initialize card list");
            System.exit(1);
        }
    }

    @Test
    public void testKirvesDeck() throws CardException {
        var kirvesDeck = new Deck();

        assertEquals(54, kirvesDeck.size());
    }

    @Test
    public void testAddingPlayers() throws CardException, KirvesGameException {
        var game = getTestGame();

        assertEquals(4, game.out(TEST_USERS.get(0)).getPlayers().size());

        //adding same player should produce exception, but shouldn't increase number of players
        try {
            game.addPlayer(TEST_USERS.get(3));
        } catch (KirvesGameException e) {
            assertEquals("Pelaaja test3@example.com on jo pelissä", e.getMessage());
        }
        assertEquals(4, game.out(TEST_USERS.get(0)).getPlayers().size());
    }

    @Test
    public void testTurnOrderAndAvailableActions() throws CardException, KirvesGameException {
        var game = getTestGame();

        var cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, false, getRandomCard(OTHER_CARDS), null);
        assertNotNull(game.getCutCard());

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), DEAL));
        game.deal(TEST_USERS.get(0), OTHER_CARDS);
        assertNull(game.getCutCard());

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), SPEAK));
        game.speak(TEST_USERS.get(1), KEEP);
        assertEquals(0, game.out().getNumOfPlayedRounds());

        var declaredPlayers = getDeclaredPlayers(game);
        assertEquals(1, declaredPlayers.size());
        assertEquals(TEST_USERS.get(1).getEmail(), declaredPlayers.get(0).getEmail());

        assertEquals("", game.out().getFirstCardSuit());
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), FOLD));
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), PLAY_CARD));
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(2), FOLD));
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(3), FOLD));
        game.playCard(TEST_USERS.get(1), 0);
        assertEquals(0, game.out().getNumOfPlayedRounds());

        var firstCardSuit = getFirstCardSuit(game);
        System.out.printf("Ajomaa: %s%n", firstCardSuit);

        assertEquals(firstCardSuit, game.out().getFirstCardSuit());
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(2), PLAY_CARD));
        game.playCard(TEST_USERS.get(2), 0);
        assertEquals(0, game.out().getNumOfPlayedRounds());

        assertEquals(firstCardSuit, game.out().getFirstCardSuit());
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(3), PLAY_CARD));
        game.playCard(TEST_USERS.get(3), 0);
        assertEquals(0, game.out().getNumOfPlayedRounds());

        assertEquals(firstCardSuit, game.out().getFirstCardSuit());
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), PLAY_CARD));
        assertEquals(4, game.out().getPlayers().stream().filter(playerOut -> !playerOut.isFolded()).count());
        game.fold(TEST_USERS.get(0));
        assertEquals(3, game.out().getPlayers().stream().filter(playerOut -> !playerOut.isFolded()).count());
        assertEquals(1, game.out().getNumOfPlayedRounds());

        assertEquals("", game.out().getFirstCardSuit());
        var out = game.out();

        System.out.printf("Valtti: %s%n", out.getTrump());
        System.out.println(getRoundCards(out));

        var winner = game.getRoundWinner(0).orElseThrow(KirvesGameException::new);
        System.out.printf("Round winner is %s%n", winner.getUserEmail());
        assertTrue(game.userHasActionAvailable(new User(winner.getUser()), PLAY_CARD));
    }

    @Test
    public void testVakyri() throws CardException, KirvesGameException {
        var game = getTestGame();

        var cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, false, getRandomCard(OTHER_CARDS), null);
        game.deal(TEST_USERS.get(0), JACKS_AND_JOKERS);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), DISCARD));
        var trump = getTrump(game.getExtraCard(TEST_USERS.get(0)));
        game.discard(TEST_USERS.get(0), 0);

        assertEquals(trump, game.getTrump());
        //check to ensure trump can not be changed
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), PLAY_CARD));

        var declaredPlayers = getDeclaredPlayers(game);

        assertEquals(1, declaredPlayers.size());
        assertEquals(TEST_USERS.get(0).getEmail(), declaredPlayers.get(0).getEmail());
    }

    @Test
    public void testYhteinen() throws CardException, KirvesGameException {
        var game = getTestGame();

        var cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, false, getRandomCard(JACKS_AND_JOKERS), getRandomCard(OTHER_CARDS));
        game.deal(TEST_USERS.get(0));

        var trumpCard = game.getExtraCard(TEST_USERS.get(0));
        var trump = trumpCard.getSuit() == JOKER ?
                (trumpCard.getRank() == BLACK ? SPADES : HEARTS) :
                trumpCard.getSuit();

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(3), DISCARD));
        game.discard(TEST_USERS.get(3), 0);
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), DISCARD));
        game.discard(TEST_USERS.get(0), 0);

        assertEquals(trump, game.getTrump());
        //check to ensure trump can not be changed
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), PLAY_CARD));

        var declaredPlayers = getDeclaredPlayers(game);

        assertEquals(2, declaredPlayers.size());
        assertTrue(declaredPlayers.stream().anyMatch(p -> p.getEmail().equals(TEST_USERS.get(0).getEmail())));
        assertTrue(declaredPlayers.stream().anyMatch(p -> p.getEmail().equals(TEST_USERS.get(3).getEmail())));
    }

    @Test
    public void testDoubleCutAcceptingSecondCut() throws CardException, KirvesGameException {
        var game = getTestGame();

        var cutCards = getRandomCards(JACKS_AND_JOKERS, 2);
        var cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, false, cutCards.get(0), cutCards.get(1));

        assertTrue(game.userHasActionAvailable(cutter, CUT));
        game.cut(cutter, false, getRandomCard(OTHER_CARDS), null);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), DEAL));
    }

    @Test
    public void testDoubleCutDecliningSecondCut() throws CardException, KirvesGameException {
        var game = getTestGame();

        var cutCards = getRandomCards(JACKS_AND_JOKERS, 2);
        var cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, false, cutCards.get(0), cutCards.get(1));

        assertTrue(game.userHasActionAvailable(cutter, CUT));
        //selecting jack or joker as cut card shouldn't matter, because player is declining cut
        game.cut(cutter, true, getRandomCard(JACKS_AND_JOKERS), null);

        assertNull(game.getExtraCard(cutter));
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), DEAL));
    }

    @Test
    public void testHakki() throws CardException, KirvesGameException {
        var game = getTestGame();

        var cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, false, getRandomCard(OTHER_CARDS), null);
        game.deal(TEST_USERS.get(0), TWOS_AND_ACES);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), ACE_OR_TWO_DECISION));
        game.aceOrTwoDecision(TEST_USERS.get(0), true);
        var trump = game.getExtraCard(TEST_USERS.get(0)).getSuit();
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), DISCARD));
        game.discard(TEST_USERS.get(0), 0);

        assertEquals(trump, game.getTrump());
        //check to ensure trump can not be changed
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), PLAY_CARD));

        var declaredPlayers = getDeclaredPlayers(game);

        assertEquals(1, declaredPlayers.size());
        assertEquals(TEST_USERS.get(0).getEmail(), declaredPlayers.get(0).getEmail());
    }

    @Test
    public void testHakkiDontKeepCard() throws CardException, KirvesGameException {
        var game = getTestGame();

        var cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, false, getRandomCard(OTHER_CARDS), null);
        game.deal(TEST_USERS.get(0), TWOS_AND_ACES);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), ACE_OR_TWO_DECISION));
        game.aceOrTwoDecision(TEST_USERS.get(0), false);
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), SPEAK));
    }

    @Test
    public void testSpeakPassing() throws CardException, KirvesGameException {
        var game = getTestGame();

        var cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, false, getRandomCard(OTHER_CARDS), null);
        game.deal(TEST_USERS.get(0), OTHER_CARDS);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), SPEAK));
        game.speak(TEST_USERS.get(1), PASS);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(2), SPEAK));
        game.speak(TEST_USERS.get(2), PASS);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(3), SPEAK));
        game.speak(TEST_USERS.get(3), PASS);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), SPEAK));
        game.speak(TEST_USERS.get(0), PASS);

        cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        assertEquals(cutter.getEmail(), TEST_USERS.get(0).getEmail());
    }

    @Test
    public void testSpeakChanging() throws CardException, KirvesGameException {
        var game = getTestGame();

        var cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, false, getRandomCard(OTHER_CARDS), null);
        game.deal(TEST_USERS.get(0), OTHER_CARDS);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), SPEAK));
        game.speak(TEST_USERS.get(1), PASS);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(2), SPEAK));
        game.speak(TEST_USERS.get(2), PASS);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(3), SPEAK));
        game.speak(TEST_USERS.get(3), CHANGE);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), SPEAK));
        game.speak(TEST_USERS.get(0), PASS);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(3), SPEAK_SUIT));
        var trump = getRandomSuitOtherThan(game.getTrump());
        assertNotEquals(trump, game.getTrump());
        game.speakSuit(TEST_USERS.get(3), trump);
        assertEquals(trump, game.getTrump());
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), PLAY_CARD));
    }

    @Test
    public void testSpeakKeeping() throws CardException, KirvesGameException {
        var game = getTestGame();

        var cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, false, getRandomCard(OTHER_CARDS), null);
        game.deal(TEST_USERS.get(0), OTHER_CARDS);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), SPEAK));
        game.speak(TEST_USERS.get(1), KEEP);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), PLAY_CARD));
    }

    @Test
    public void testSpeakKeepAfterWantingChange() throws CardException, KirvesGameException {
        var game = getTestGame();

        var cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, false, getRandomCard(OTHER_CARDS), null);
        game.deal(TEST_USERS.get(0), OTHER_CARDS);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), SPEAK));
        game.speak(TEST_USERS.get(1), CHANGE);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(2), SPEAK));
        game.speak(TEST_USERS.get(2), KEEP);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), PLAY_CARD));
    }

    @Test
    public void testPlayingThroughThreeHands() throws CardException, KirvesGameException {
        //currently there is no easy way to test more than three hands
        var game = getTestGame();

        for (User dealer : TEST_USERS.subList(0, 3)) {
            var cutCard = getRandomCard(OTHER_CARDS);
            var cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
            game.cut(cutter, false, cutCard, null);
            assertNotNull(game.getCutCard());
            assertEquals(0, game.out().getPlayers().stream().mapToInt(p -> p.getPlayedCards().size()).sum());

            var possibleTrumpCards = new ArrayList<>(OTHER_CARDS);
            possibleTrumpCards.remove(cutCard);
            assertTrue(game.userHasActionAvailable(dealer, DEAL));
            game.deal(dealer, possibleTrumpCards);
            game.getUserWithAction(SPEAK).ifPresent(player -> {
                assertTrue(game.userHasActionAvailable(player, SPEAK));
                try {
                    game.speak(player, KEEP);
                } catch (KirvesGameException e) {
                    fail("Failed to speak");
                }
            });
            playThroughHand(game, TEST_USERS);
        }
    }

    @Test
    public void testWinningCards() throws CardException {
        //samaa maata, isompi voittaa
        var cards = List.of(new Card(SPADES, SEVEN), new Card(SPADES, TEN));
        assertEquals(cards.get(1), Game.winningCard(cards, DIAMONDS));

        //eri maata, ajokortti voittaa
        cards = List.of(new Card(SPADES, SEVEN), new Card(CLUBS, TEN));
        assertEquals(cards.get(0), Game.winningCard(cards, DIAMONDS));

        //valtti voittaa, vaikkaa on pienempi
        cards = List.of(new Card(SPADES, SEVEN), new Card(CLUBS, TWO));
        assertEquals(cards.get(1), Game.winningCard(cards, CLUBS));

        //pamppu voittaa valttiässän
        cards = List.of(new Card(SPADES, ACE), new Card(SPADES, JACK));
        assertEquals(cards.get(1), Game.winningCard(cards, SPADES));

        //pamppu voittaa hantin (lasketaan valtiksi)
        cards = List.of(new Card(CLUBS, ACE), new Card(SPADES, JACK));
        assertEquals(cards.get(1), Game.winningCard(cards, HEARTS));

        //punainen jokeri voittaa mustan
        cards = List.of(new Card(JOKER, BLACK), new Card(JOKER, RED));
        assertEquals(cards.get(1), Game.winningCard(cards, SPADES));

        //jokeri voittaa pampun
        cards = List.of(new Card(CLUBS, JACK), new Card(JOKER, BLACK));
        assertEquals(cards.get(1), Game.winningCard(cards, SPADES));
    }

    @Test
    public void testHandWinner() throws KirvesGameException {
        var firstTwoPOJO = new PlayerPOJO();
        firstTwoPOJO.user = new UserPOJO("firstTwo", "firstTwo");
        firstTwoPOJO.roundsWon.addAll(List.of(0, 1));

        var thirdPOJO = new PlayerPOJO();
        thirdPOJO.user = new UserPOJO("third", "third");
        thirdPOJO.roundsWon.addAll(List.of(2));

        var lastTwoPOJO = new PlayerPOJO();
        lastTwoPOJO.user = new UserPOJO("lastTwo", "lastTwo");
        lastTwoPOJO.roundsWon.addAll(List.of(3,4));

        var lastThreePOJO = new PlayerPOJO();
        lastThreePOJO.user = new UserPOJO("lastThree", "lastThree");
        lastThreePOJO.roundsWon.addAll(List.of(2,3,4));

        //three should win
        var winner = Game.determineHandWinner(List.of(
                new Player(firstTwoPOJO, null),
                new Player(lastThreePOJO, null)));
        assertEquals(winner.getUserEmail(), lastThreePOJO.user.email);

        //two earlier should win
        winner = Game.determineHandWinner(List.of(
                new Player(firstTwoPOJO, null),
                new Player(thirdPOJO, null),
                new Player(lastTwoPOJO, null)));
        assertEquals(winner.getUserEmail(), firstTwoPOJO.user.email);
    }
    
    @Test
    public void testScoringWinnersLogic() {
        var pojo1 = new PlayerPOJO();
        pojo1.user = new UserPOJO("first", "first");
        pojo1.declaredPlayer = true;
        var p1 = new Player(pojo1, null);

        var pojo2 = new PlayerPOJO();
        pojo2.user = new UserPOJO("second", "second");
        pojo2.declaredPlayer = false;
        var p2 = new Player(pojo2, p1);

        var pojo3 = new PlayerPOJO();
        pojo3.user = new UserPOJO("third", "third");
        pojo3.declaredPlayer = false;
        var p3 = new Player(pojo3, p2);

        var pojo4 = new PlayerPOJO();
        pojo4.user = new UserPOJO("fourth", "fourth");
        pojo4.declaredPlayer = true;
        var p4 = new Player(pojo4, p3);
        p1.setPrevious(p4);
        p4.setNext(p1);

        var declaredPlayerWins = Game.determineScoringWinners(List.of(p1, p2, p3), p1);
        assertEquals(1, declaredPlayerWins.size());
        assertTrue(declaredPlayerWins.contains(p1));

        var declaredPlayerLoses = Game.determineScoringWinners(List.of(p1,p2,p3), p2);
        assertEquals(2, declaredPlayerLoses.size());
        assertTrue(declaredPlayerLoses.containsAll(List.of(p2, p3)));

        var yhteinenDeclaredPlayerWins = Game.determineScoringWinners(List.of(p1,p2,p3,p4), p1);
        assertEquals(3, yhteinenDeclaredPlayerWins.size());
        assertTrue(yhteinenDeclaredPlayerWins.containsAll(List.of(p1,p2,p3)));

        var yhteinenOtherPlayerWins = Game.determineScoringWinners(List.of(p1,p2,p3,p4), p2);
        assertEquals(2, yhteinenOtherPlayerWins.size());
        assertTrue(yhteinenOtherPlayerWins.containsAll(List.of(p2,p3)));
    }

    @Test
    public void testGetRandomCards() throws CardException {
        assertEquals(6, JACKS_AND_JOKERS.size());
        assertEquals(8, TWOS_AND_ACES.size());
        assertEquals(40, OTHER_CARDS.size());

        var jackOrJoker = getRandomCard(JACKS_AND_JOKERS);
        assertTrue(JACKS_AND_JOKERS.contains(jackOrJoker));

        var twoOrAce = getRandomCard(TWOS_AND_ACES);
        assertTrue(TWOS_AND_ACES.contains(twoOrAce));

        var otherCard = getRandomCard(OTHER_CARDS);
        assertTrue(OTHER_CARDS.contains(otherCard));

        var list = getRandomCards(OTHER_CARDS, 2);
        assertEquals(2, list.size());
        assertTrue(OTHER_CARDS.contains(list.get(0)));
        assertTrue(OTHER_CARDS.contains(list.get(1)));
    }

    @Test
    public void testOutputByUser() throws CardException, KirvesGameException {
        var game = getTestGame();

        var output1 = game.out(TEST_USERS.get(0));
        assertEquals(4, output1.getPlayers().size());
        assertEquals(TEST_USERS.get(0).getEmail(), output1.getPlayers().get(0).getEmail());

        var output2 = game.out(TEST_USERS.get(2));
        assertEquals(4, output2.getPlayers().size());
        assertEquals(TEST_USERS.get(2).getEmail(), output2.getPlayers().get(0).getEmail());

        var output3 = game.out();
        assertEquals(4, output3.getPlayers().size());

        var cutCard = getRandomCard(OTHER_CARDS);
        game.cut(TEST_USERS.get(3), false, cutCard, null);
        var output4 = game.out();
        assertEquals(cutCard.toString(), output4.getCutCard());
    }

    @Test
    public void testPOJO() throws CardException, KirvesGameException {
        var game = getTestGame();

        var json = game.toJson();
        var pojo = JsonUtil.getJavaObject(json, GameDataPOJO.class).orElse(null);
        assertNotNull(pojo);
        assertEquals(4, pojo.players.size());

        game.cut(TEST_USERS.get(3), false, getRandomCard(OTHER_CARDS), null);
        game.deal(TEST_USERS.get(0), OTHER_CARDS);

        json = game.toJson();
        pojo = JsonUtil.getJavaObject(json, GameDataPOJO.class).orElse(null);
        assertNotNull(pojo);
        assertEquals(4, pojo.players.size());

        game = new Game(pojo);
        assertEquals(4, game.getNumberOfPlayers());
    }

    @Test
    public void testPOJOWithJsonUtil() throws CardException, KirvesGameException {
        var game = getTestGame();

        var json = game.toJson();
        var pojoOut = JsonUtil.getJavaObject(json, GameDataPOJO.class).orElseThrow();
        var gameFromPojo = new Game(pojoOut);

        assertEquals(game, gameFromPojo);
    }

    @Test
    public void testCanFold() {
        var p1pojo = new PlayerPOJO();
        p1pojo.hand = List.of("3H", "4S", "6C", "TH", "JD");
        p1pojo.user = new UserPOJO("p1", "p1");

        var p2pojo = new PlayerPOJO();
        p2pojo.hand = List.of("4C", "7C", "TC", "KS", "AS");
        p2pojo.user = new UserPOJO("p2", "p2");

        var p1 = new Player(p1pojo, null);
        var p2 = new Player(p2pojo, p1);
        p1.setPrevious(p2);
        p2.setNext(p1);

        //should be able to fold: no cards played
        assertTrue(Game.canFold(p1, p2, p2, DIAMONDS, 4));

        //should not able to fold: situation is not "between rounds"
        assertFalse(Game.canFold(p1, p2, p1, DIAMONDS, 4));

        p1pojo.hand = List.of("3H", "4S", "6C", "TH");
        p1 = new Player(p1pojo, null);

        //should be able to fold: cards played, no valtti in hand
        assertTrue(Game.canFold(p1, p2, p2, DIAMONDS, 4));

        //should not be able to fold: cards played, valtti in hand
        assertFalse(Game.canFold(p1, p2, p2, HEARTS, 4));

        //should be able to fold: declared player
        p1pojo.hand = List.of("3H", "4S", "6C", "TH", "JD");
        p1pojo.declaredPlayer = true;
        p1 = new Player(p1pojo, null);
        assertTrue(Game.canFold(p1, p2, p2, HEARTS, 4));

        //should not be able to fold: you are first player of round
        assertFalse(Game.canFold(p2, p2, p2, HEARTS, 4));
    }

    @Test
    public void testGame18Bug() throws IOException, KirvesGameException {
        var json = FileUtils.readFileToString(new File("src/test/resources/game18.json"), "UTF-8");
        var pojo = JsonUtil.getJavaObject(json, GameDataPOJO.class).orElseThrow();

        var game = new Game(pojo);
        assertEquals(3, game.getNumberOfPlayers());
        var p2 = new User(pojo.players.get(1).user);

        assertTrue(game.userHasActionAvailable(p2, FOLD));
        game.fold(p2);
    }

    //--------------------------
    //PRIVATE METHODS START HERE
    //--------------------------

    private static String getFirstCardSuit(Game game) throws KirvesGameException {
        return game.getPlayer(TEST_USERS.get(1).getEmail())
                .map(player -> {
                    var card = player.getLastPlayedCard();
                    if (card.getSuit() == JOKER || card.getRank() == JACK) {
                        return game.getTrump().name();
                    } else {
                        return card.getSuit().name();
                    }
                })
                .orElseThrow(KirvesGameException::new);
    }

    private static List<PlayerOut> getDeclaredPlayers(Game game) throws KirvesGameException {
        return game.out().getPlayers().stream()
                .filter(PlayerOut::isDeclaredPlayer)
                .collect(Collectors.toList());
    }

    private static Card.Suit getRandomSuitOtherThan(Card.Suit suit) throws CardException {
        if(suit == JOKER) throw new CardException("JOKER is not allowed as suit here");
        var suits = new ArrayList<>(Set.of(SPADES, HEARTS, CLUBS, DIAMONDS));
        suits.remove(suit);
        Collections.shuffle(suits);
        return suits.get(0);
    }

    private static Card.Suit getTrump(Card card) {
        if(card.getSuit() == JOKER) {
            return card.getRank() == BLACK ? SPADES : HEARTS;
        } else {
            return card.getSuit();
        }
    }

    private static Card getRandomCard(List<Card> cards) throws CardException {
        return getRandomCards(cards, 1).get(0);
    }

    private static List<Card> getRandomCards(List<Card> cards, int count) throws CardException {
        if(count > cards.size()) {
            throw new CardException("Can't get more cards than list has");
        }
        var mutableList = new ArrayList<>(cards);
        Collections.shuffle(mutableList);
        return mutableList.subList(0, count);
    }

    private static void playThroughHand(Game game, List<User> players) throws CardException, KirvesGameException {
        for(int i = 0; i < 5; i++) {
            playRound(game, players);
        }
    }

    private static void playRound(Game game, List<User> players) throws KirvesGameException, CardException {
        var turn = players.stream()
                .filter(user -> game.userHasActionAvailable(user, PLAY_CARD))
                .findFirst()
                .orElseThrow(KirvesGameException::new);
        var index = players.indexOf(turn);
        var nextRoundUsers = new ArrayList<>(players.subList(index, players.size()));
        if(index > 0) {
            nextRoundUsers.addAll(players.subList(0, index));
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
    }

    private static Game getTestGame() throws CardException, KirvesGameException {
        return getTestGame(TEST_USERS);
    }

    private static Game getTestGame(List<User> users) throws CardException, KirvesGameException {
        var game = new Game(users.get(0));
        if(users.size() > 1) {
            for(int i = 1; i < users.size(); i++) {
                game.addPlayer(users.get(i));
            }
        }
        return game;
    }

    private static String getRoundCards(GameOut out) {
        var players = out.getPlayers();
        var sb = new StringBuilder();
        for(int i = 0; i < players.size(); i++) {
            var player = players.get(i);
            sb.append(player.getEmail());
            sb.append(": ");
            if(player.isFolded()) {
                sb.append("FOLDED");
            }
            else {
                sb.append(player.getPlayedCards().get(0));
                if (player.getRoundsWon().contains(0)) {
                    sb.append(" X");
                }
            }
            if(i < players.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
