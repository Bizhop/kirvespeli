package fi.bizhop.jassu.model;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;
import fi.bizhop.jassu.util.RandomUtil;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static fi.bizhop.jassu.model.Card.Rank.*;
import static fi.bizhop.jassu.model.Card.Suit.*;
import static fi.bizhop.jassu.model.KirvesGame.Action.*;
import static org.junit.Assert.*;

public class KirvesTest {
    static final List<User> TEST_USERS;

    static List<Card> JACKS_AND_JOKERS;
    static List<Card> OTHER_CARDS;
    static List<Card> TWOS_AND_ACES;

    static {
        TEST_USERS = List.of(
                new User("test1@example.com", ""),
                new User("test2@example.com", ""),
                new User("test3@example.com", ""),
                new User("test4@example.com", ""));

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
            assertEquals("Player test4@example.com already joined game", e.getMessage());
        }
        assertEquals(4, game.out(TEST_USERS.get(0)).getPlayers().size());
    }

    @Test
    public void testTurnOrderAndAvailableActions() throws CardException, KirvesGameException {
        KirvesGame game = getTestGame(TEST_USERS);

        User cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, false, getRandomCard(OTHER_CARDS), null);
        assertNotNull(game.getCutCard());

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), DEAL));
        game.deal(TEST_USERS.get(0), OTHER_CARDS);
        assertNull(game.getCutCard());

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), SET_VALTTI));
        game.setValtti(TEST_USERS.get(1), game.getValtti(), TEST_USERS.get(1));
        assertEquals(0, game.out(null).getNumOfPlayedRounds());

        List<KirvesPlayerOut> declaredPlayers = getDeclaredPlayers(game);
        assertEquals(1, declaredPlayers.size());
        assertEquals(TEST_USERS.get(1).getEmail(), declaredPlayers.get(0).getEmail());

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
    public void testVakyri() throws CardException, KirvesGameException {
        KirvesGame game = getTestGame(TEST_USERS);

        User cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, false, getRandomCard(OTHER_CARDS), null);
        game.deal(TEST_USERS.get(0), JACKS_AND_JOKERS);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), DISCARD));
        Card.Suit valtti = getValtti(game.getExtraCard(TEST_USERS.get(0)));
        game.discard(TEST_USERS.get(0), 0);

        assertEquals(valtti, game.getValtti());
        //check to ensure valtti can not be changed
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), PLAY_CARD));

        List<KirvesPlayerOut> declaredPlayers = getDeclaredPlayers(game);

        assertEquals(1, declaredPlayers.size());
        assertEquals(TEST_USERS.get(0).getEmail(), declaredPlayers.get(0).getEmail());
    }

    @Test
    public void testYhteinen() throws CardException, KirvesGameException {
        KirvesGame game = getTestGame(TEST_USERS);

        User cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, false, getRandomCard(JACKS_AND_JOKERS), getRandomCard(OTHER_CARDS));
        game.deal(TEST_USERS.get(0));

        Card.Suit valtti = game.getExtraCard(TEST_USERS.get(0)).getSuit();

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(3), DISCARD));
        game.discard(TEST_USERS.get(3), 0);
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), DISCARD));
        game.discard(TEST_USERS.get(0), 0);

        assertEquals(valtti, game.getValtti());
        //check to ensure valtti can not be changed
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), PLAY_CARD));

        List<KirvesPlayerOut> declaredPlayers = getDeclaredPlayers(game);

        assertEquals(2, declaredPlayers.size());
        assertTrue(declaredPlayers.stream().anyMatch(p -> p.getEmail().equals(TEST_USERS.get(0).getEmail())));
        assertTrue(declaredPlayers.stream().anyMatch(p -> p.getEmail().equals(TEST_USERS.get(3).getEmail())));
    }

    @Test
    public void testDoubleCutAcceptingSecondCut() throws CardException, KirvesGameException {
        KirvesGame game = getTestGame(TEST_USERS);

        List<Card> cutCards = getRandomCards(JACKS_AND_JOKERS, 2);
        User cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, false, cutCards.get(0), cutCards.get(1));

        assertTrue(game.userHasActionAvailable(cutter, CUT));
        game.cut(cutter, false, getRandomCard(OTHER_CARDS), null);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), DEAL));
    }

    @Test
    public void testDoubleCutDecliningSecondCut() throws CardException, KirvesGameException {
        KirvesGame game = getTestGame(TEST_USERS);

        List<Card> cutCards = getRandomCards(JACKS_AND_JOKERS, 2);
        User cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, false, cutCards.get(0), cutCards.get(1));

        assertTrue(game.userHasActionAvailable(cutter, CUT));
        //selecting jack or joker as cut card shouldn't matter, because player is declining cut
        game.cut(cutter, true, getRandomCard(JACKS_AND_JOKERS), null);

        assertNull(game.getExtraCard(cutter));
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), DEAL));
    }

    @Test
    public void testHakki() throws CardException, KirvesGameException {
        KirvesGame game = getTestGame(TEST_USERS);

        User cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, false, getRandomCard(OTHER_CARDS), null);
        game.deal(TEST_USERS.get(0), TWOS_AND_ACES);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), ACE_OR_TWO_DECISION));
        game.aceOrTwoDecision(TEST_USERS.get(0), true);
        Card.Suit valtti = game.getExtraCard(TEST_USERS.get(0)).getSuit();
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), DISCARD));
        game.discard(TEST_USERS.get(0), 0);

        assertEquals(valtti, game.getValtti());
        //check to ensure valtti can not be changed
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), PLAY_CARD));

        List<KirvesPlayerOut> declaredPlayers = getDeclaredPlayers(game);

        assertEquals(1, declaredPlayers.size());
        assertEquals(TEST_USERS.get(0).getEmail(), declaredPlayers.get(0).getEmail());
    }

    @Test
    public void testHakkiDontKeepCard() throws CardException, KirvesGameException {
        KirvesGame game = getTestGame(TEST_USERS);

        User cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, false, getRandomCard(OTHER_CARDS), null);
        game.deal(TEST_USERS.get(0), TWOS_AND_ACES);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), ACE_OR_TWO_DECISION));
        game.aceOrTwoDecision(TEST_USERS.get(0), false);
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), SET_VALTTI));
    }

    @Test
    public void testSetValtti() throws CardException, KirvesGameException {
        KirvesGame game = getTestGame(TEST_USERS);

        User cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, false, getRandomCard(OTHER_CARDS), null);
        game.deal(TEST_USERS.get(0), OTHER_CARDS);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), SET_VALTTI));
        List<Card.Suit> suits = new ArrayList<>(Set.of(HEARTS, CLUBS, SPADES, DIAMONDS));
        suits.remove(game.getValtti());
        Card.Suit newValtti = suits.get(RandomUtil.getInt(suits.size()));
        game.setValtti(TEST_USERS.get(1), newValtti, TEST_USERS.get(2));

        List<KirvesPlayerOut> declaredPlayers = getDeclaredPlayers(game);
        assertEquals(1, declaredPlayers.size());
        assertEquals(TEST_USERS.get(2).getEmail(), declaredPlayers.get(0).getEmail());

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), PLAY_CARD));
        assertEquals(newValtti, game.getValtti());
    }

    @Test
    public void testPassing() throws CardException, KirvesGameException {
        KirvesGame game = getTestGame(TEST_USERS);

        User cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, false, getRandomCard(OTHER_CARDS), null);
        game.deal(TEST_USERS.get(0), OTHER_CARDS);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), SET_VALTTI));
        game.startNextRound(TEST_USERS.get(1));

        cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        assertEquals(TEST_USERS.get(0), cutter);

        int cardsInHands = game.out().getPlayers().stream()
                .mapToInt(KirvesPlayerOut::getCardsInHand)
                .sum();
        assertEquals(0, cardsInHands);
    }

    @Test
    public void testKeepValtti() throws CardException, KirvesGameException {
        KirvesGame game = getTestGame(TEST_USERS);

        User cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
        game.cut(cutter, false, getRandomCard(OTHER_CARDS), null);
        game.deal(TEST_USERS.get(0), OTHER_CARDS);

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), SET_VALTTI));
        Card.Suit valtti = game.getValtti();
        game.setValtti(TEST_USERS.get(1), valtti, TEST_USERS.get(3));

        List<KirvesPlayerOut> declaredPlayers = getDeclaredPlayers(game);
        assertEquals(1, declaredPlayers.size());
        assertEquals(TEST_USERS.get(3).getEmail(), declaredPlayers.get(0).getEmail());

        assertNotNull(game.getValttiCard());
        assertEquals(valtti, game.getValtti());
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(1), PLAY_CARD));
    }

    @Test
    public void testAdjustingPlayers() throws CardException, KirvesGameException {
        KirvesGame game = getTestGame(TEST_USERS);

        game.cut(TEST_USERS.get(3), false, getRandomCard(OTHER_CARDS), null);
        game.deal(TEST_USERS.get(0), OTHER_CARDS);
        game.setValtti(TEST_USERS.get(1), getRandomCard(OTHER_CARDS).getSuit(), TEST_USERS.get(3));
        playThroughHand(game, TEST_USERS);

        assertEquals(4, game.out(TEST_USERS.get(0)).getPlayers().size());
        assertEquals(4, game.out(TEST_USERS.get(0)).getPlayersTotal().longValue());

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), ADJUST_PLAYERS_IN_GAME));
        game.adjustPlayersInGame(TEST_USERS.get(0), false, Set.of(TEST_USERS.get(1).getEmail(), TEST_USERS.get(2).getEmail()));

        assertEquals(2, game.out(TEST_USERS.get(0)).getPlayers().size());
        assertEquals(4, game.out(TEST_USERS.get(0)).getPlayersTotal().longValue());

        assertTrue(game.userHasActionAvailable(TEST_USERS.get(0), CUT));
        game.cut(TEST_USERS.get(0), false, getRandomCard(OTHER_CARDS), null);
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(3), DEAL));
        game.deal(TEST_USERS.get(3), OTHER_CARDS);
        game.setValtti(TEST_USERS.get(0), getRandomCard(OTHER_CARDS).getSuit(), TEST_USERS.get(0));

        playThroughHand(game, List.of(TEST_USERS.get(0), TEST_USERS.get(3)));
        assertTrue(game.userHasActionAvailable(TEST_USERS.get(3), ADJUST_PLAYERS_IN_GAME));
        game.adjustPlayersInGame(TEST_USERS.get(3), true, null);

        assertEquals(4, game.out(TEST_USERS.get(0)).getPlayers().size());
        assertEquals(4, game.out(TEST_USERS.get(0)).getPlayersTotal().longValue());
    }

    @Test
    public void testPlayingThroughFourHands() throws CardException, KirvesGameException {
        KirvesGame game = getTestGame(TEST_USERS);

        for (User dealer : TEST_USERS) {
            Card cutCard = getRandomCard(OTHER_CARDS);
            User cutter = game.getUserWithAction(CUT).orElseThrow(KirvesGameException::new);
            game.cut(cutter, false, cutCard, null);
            assertNotNull(game.getCutCard());

            List<Card> possibleValttiCards = new ArrayList<>(OTHER_CARDS);
            possibleValttiCards.remove(cutCard);
            assertTrue(game.userHasActionAvailable(dealer, DEAL));
            game.deal(dealer, possibleValttiCards);
            game.getUserWithAction(SET_VALTTI).ifPresent(player -> {
                assertTrue(game.userHasActionAvailable(player, SET_VALTTI));
                try {
                    game.setValtti(player, game.getValtti(), player);
                } catch (KirvesGameException e) {
                    fail("Failed to set valtti");
                }
            });
            playThroughHand(game, TEST_USERS);
        }
    }

    @Test
    public void testWinningCards() throws CardException {
        //samaa maata, isompi voittaa
        List<Card> cards = List.of(new Card(SPADES, SEVEN), new Card(SPADES, TEN));
        assertEquals(cards.get(1), KirvesGame.winningCard(cards, DIAMONDS));

        //eri maata, ajokortti voittaa
        cards = List.of(new Card(SPADES, SEVEN), new Card(CLUBS, TEN));
        assertEquals(cards.get(0), KirvesGame.winningCard(cards, DIAMONDS));

        //valtti voittaa, vaikkaa on pienempi
        cards = List.of(new Card(SPADES, SEVEN), new Card(CLUBS, TWO));
        assertEquals(cards.get(1), KirvesGame.winningCard(cards, CLUBS));

        //pamppu voittaa valttiässän
        cards = List.of(new Card(SPADES, ACE), new Card(SPADES, JACK));
        assertEquals(cards.get(1), KirvesGame.winningCard(cards, SPADES));

        //pamppu voittaa hantin (lasketaan valtiksi)
        cards = List.of(new Card(CLUBS, ACE), new Card(SPADES, JACK));
        assertEquals(cards.get(1), KirvesGame.winningCard(cards, HEARTS));

        //punainen jokeri voittaa mustan
        cards = List.of(new Card(JOKER, BLACK), new Card(JOKER, RED));
        assertEquals(cards.get(1), KirvesGame.winningCard(cards, SPADES));

        //jokeri voittaa pampun
        cards = List.of(new Card(CLUBS, JACK), new Card(JOKER, BLACK));
        assertEquals(cards.get(1), KirvesGame.winningCard(cards, SPADES));
    }

    @Test
    public void testGetRandomCards() throws CardException {
        assertEquals(6, JACKS_AND_JOKERS.size());
        assertEquals(8, TWOS_AND_ACES.size());
        assertEquals(40, OTHER_CARDS.size());

        Card jackOrJoker = getRandomCard(JACKS_AND_JOKERS);
        assertTrue(JACKS_AND_JOKERS.contains(jackOrJoker));

        Card twoOrAce = getRandomCard(TWOS_AND_ACES);
        assertTrue(TWOS_AND_ACES.contains(twoOrAce));

        Card otherCard = getRandomCard(OTHER_CARDS);
        assertTrue(OTHER_CARDS.contains(otherCard));

        List<Card> list = getRandomCards(OTHER_CARDS, 2);
        assertEquals(2, list.size());
        assertTrue(OTHER_CARDS.contains(list.get(0)));
        assertTrue(OTHER_CARDS.contains(list.get(1)));
    }

    @Test
    public void testOutputByUser() throws CardException, KirvesGameException {
        KirvesGame game = getTestGame(TEST_USERS);

        KirvesGameOut output1 = game.out(TEST_USERS.get(0));
        assertEquals(4, output1.getPlayers().size());
        assertEquals(TEST_USERS.get(0).getEmail(), output1.getPlayers().get(0).getEmail());

        KirvesGameOut output2 = game.out(TEST_USERS.get(2));
        assertEquals(4, output2.getPlayers().size());
        assertEquals(TEST_USERS.get(2).getEmail(), output2.getPlayers().get(0).getEmail());

        KirvesGameOut output3 = game.out(null);
        assertEquals(4, output3.getPlayers().size());

        Card cutCard = getRandomCard(OTHER_CARDS);
        game.cut(TEST_USERS.get(3), false, cutCard, null);
        KirvesGameOut output4 = game.out(null);
        assertEquals(cutCard.toString(), output4.getCutCard());
    }


    //--------------------------
    //PRIVATE METHODS START HERE
    //--------------------------

    private List<KirvesPlayerOut> getDeclaredPlayers(KirvesGame game) throws KirvesGameException {
        return game.out().getPlayers().stream()
                .filter(KirvesPlayerOut::isDeclaredPlayer)
                .collect(Collectors.toList());
    }


    private Card.Suit getValtti(Card card) {
        if(card.getSuit() == JOKER) {
            return card.getRank() == BLACK ? SPADES : HEARTS;
        } else {
            return card.getSuit();
        }
    }

    private Card getRandomCard(List<Card> cards) throws CardException {
        return getRandomCards(cards, 1).get(0);
    }

    private List<Card> getRandomCards(List<Card> cards, int count) throws CardException {
        if(count > cards.size()) {
            throw new CardException("Can't get more cards than list has");
        }
        List<Card> mutableList = new ArrayList<>(cards);
        Collections.shuffle(mutableList);
        return mutableList.subList(0, count);
    }

    private void playThroughHand(KirvesGame game, List<User> players) throws CardException, KirvesGameException {
        for(int i = 0; i < 5; i++) {
            playRound(game, players, false);
        }
    }

    private void playRound(KirvesGame game, List<User> players, boolean printMessage) throws KirvesGameException, CardException {
        User turn = players.stream()
                .filter(user -> game.userHasActionAvailable(user, PLAY_CARD))
                .findFirst()
                .orElseThrow(KirvesGameException::new);
        int index = players.indexOf(turn);
        List<User> nextRoundUsers = new ArrayList<>(players.subList(index, players.size()));
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
        if(printMessage) {
            System.out.println(game.getMessage());
        }
    }

    private KirvesGame getTestGame(List<User> players) throws CardException, KirvesGameException {
        if(players == null || players.isEmpty()) {
            throw new KirvesGameException("TEST: you must define at least one player");
        }
        KirvesGame game = new KirvesGame(players.get(0));
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
