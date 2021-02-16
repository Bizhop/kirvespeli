package fi.bizhop.jassu;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.models.Card;
import fi.bizhop.jassu.models.Cards;
import fi.bizhop.jassu.models.StandardDeck;
import fi.bizhop.jassu.util.PokerHandEvaluator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static fi.bizhop.jassu.models.Card.Rank.*;
import static fi.bizhop.jassu.models.Card.Suit.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PokerTest {
    @Test
    public void testPair() throws CardException {
        List<Card> pair = new ArrayList<>();
        pair.add(new Card(SPADES, ACE));
        pair.add(new Card(HEARTS, ACE));
        Cards hand = new Cards(pair);
        assertTrue(PokerHandEvaluator.checkPair(hand) > 0);

        List<Card> notPair = new ArrayList<>();
        notPair.add(new Card(SPADES, ACE));
        notPair.add(new Card(SPADES, TWO));
        Cards hand2 = new Cards(notPair);
        assertFalse(PokerHandEvaluator.checkPair(hand2) > 0);

        Cards deck = new StandardDeck();
        assertTrue(PokerHandEvaluator.checkPair(deck) > 0);
    }

    @Test
    public void testThree() throws CardException {
        List<Card> three = new ArrayList<>();
        three.add(new Card(SPADES, ACE));
        three.add(new Card(HEARTS, ACE));
        three.add(new Card(CLUBS, ACE));
        Cards hand = new Cards(three);
        assertTrue(PokerHandEvaluator.checkThreeOfAKind(hand) > 0);

        List<Card> notThree = new ArrayList<>();
        notThree.add(new Card(SPADES, ACE));
        notThree.add(new Card(HEARTS, ACE));
        notThree.add(new Card(CLUBS, TWO));
        Cards hand2 = new Cards(notThree);
        assertFalse(PokerHandEvaluator.checkThreeOfAKind(hand2) > 0);

        Cards deck = new StandardDeck();
        assertTrue(PokerHandEvaluator.checkThreeOfAKind(deck) > 0);
    }

    @Test
    public void testTwoPair() throws CardException {
        List<Card> twoPair = new ArrayList<>();
        twoPair.add(new Card(SPADES, ACE));
        twoPair.add(new Card(HEARTS, ACE));
        twoPair.add(new Card(SPADES, KING));
        twoPair.add(new Card(HEARTS, KING));
        Cards hand = new Cards(twoPair);
        assertTrue(PokerHandEvaluator.checkTwoPair(hand) > 0);

        List<Card> notTwoPair = new ArrayList<>();
        notTwoPair.add(new Card(SPADES, ACE));
        notTwoPair.add(new Card(HEARTS, ACE));
        notTwoPair.add(new Card(SPADES, KING));
        notTwoPair.add(new Card(HEARTS, SEVEN));
        Cards hand2 = new Cards(notTwoPair);
        assertFalse(PokerHandEvaluator.checkTwoPair(hand2) > 0);

        Cards deck = new StandardDeck();
        assertTrue(PokerHandEvaluator.checkTwoPair(deck) > 0);
    }

    @Test
    public void testStraight() throws CardException {
        List<Card> straight = new ArrayList<>();
        straight.add(new Card(HEARTS, KING));
        straight.add(new Card(SPADES, QUEEN));
        straight.add(new Card(HEARTS, JACK));
        straight.add(new Card(CLUBS, TEN));
        straight.add(new Card(SPADES, NINE));
        Cards hand = new Cards(straight);
        assertTrue(PokerHandEvaluator.checkStraight(hand) > 0);

        List<Card> specialStraight = new ArrayList<>();
        specialStraight.add(new Card(SPADES, ACE));
        specialStraight.add(new Card(HEARTS, TWO));
        specialStraight.add(new Card(SPADES, THREE));
        specialStraight.add(new Card(HEARTS, FOUR));
        specialStraight.add(new Card(CLUBS, FIVE));
        Cards hand2 = new Cards(specialStraight);
        assertTrue(PokerHandEvaluator.checkStraight(hand2) > 0);

        List<Card> notStraight = new ArrayList<>();
        notStraight.add(new Card(HEARTS, KING));
        notStraight.add(new Card(SPADES, QUEEN));
        notStraight.add(new Card(HEARTS, JACK));
        notStraight.add(new Card(CLUBS, TEN));
        notStraight.add(new Card(SPADES, SIX));
        Cards hand3 = new Cards(notStraight);
        assertFalse(PokerHandEvaluator.checkStraight(hand3) > 0);

        Cards deck = new StandardDeck();
        assertTrue(PokerHandEvaluator.checkStraight(deck) > 0);
    }

    @Test
    public void testFullHouse() throws CardException {
        List<Card> fullHouse = new ArrayList<>();
        fullHouse.add(new Card(SPADES, ACE));
        fullHouse.add(new Card(HEARTS, ACE));
        fullHouse.add(new Card(CLUBS, ACE));
        fullHouse.add(new Card(DIAMONDS, KING));
        fullHouse.add(new Card(HEARTS, KING));
        Cards hand = new Cards(fullHouse);
        assertTrue(PokerHandEvaluator.checkFullHouse(hand) > 0);

        List<Card> notFullHouse = new ArrayList<>();
        notFullHouse.add(new Card(SPADES, ACE));
        notFullHouse.add(new Card(HEARTS, ACE));
        notFullHouse.add(new Card(CLUBS, TWO));
        notFullHouse.add(new Card(CLUBS, SEVEN));
        notFullHouse.add(new Card(CLUBS, FIVE));
        Cards hand2 = new Cards(notFullHouse);
        assertFalse(PokerHandEvaluator.checkFullHouse(hand2) > 0);

        Cards deck = new StandardDeck();
        assertTrue(PokerHandEvaluator.checkFullHouse(deck) > 0);
    }

    @Test
    public void testFour() throws CardException {
        List<Card> four = new ArrayList<>();
        four.add(new Card(SPADES, ACE));
        four.add(new Card(HEARTS, ACE));
        four.add(new Card(CLUBS, ACE));
        four.add(new Card(DIAMONDS, ACE));
        Cards hand = new Cards(four);
        assertTrue(PokerHandEvaluator.checkFourOfAKind(hand) > 0);

        List<Card> notFour = new ArrayList<>();
        notFour.add(new Card(SPADES, ACE));
        notFour.add(new Card(HEARTS, ACE));
        notFour.add(new Card(CLUBS, TWO));
        Cards hand2 = new Cards(notFour);
        assertFalse(PokerHandEvaluator.checkFourOfAKind(hand2) > 0);

        Cards deck = new StandardDeck();
        assertTrue(PokerHandEvaluator.checkFourOfAKind(deck) > 0);
    }

    @Test
    public void testStraightFlush() throws CardException {
        List<Card> straightFlush = new ArrayList<>();
        straightFlush.add(new Card(SPADES, ACE));
        straightFlush.add(new Card(SPADES, KING));
        straightFlush.add(new Card(SPADES, QUEEN));
        straightFlush.add(new Card(SPADES, JACK));
        straightFlush.add(new Card(SPADES, TEN));
        Cards hand = new Cards(straightFlush);
        assertTrue(PokerHandEvaluator.checkStraightFlush(hand) > 0);

        List<Card> notStraightFlush = new ArrayList<>();
        notStraightFlush.add(new Card(SPADES, ACE));
        notStraightFlush.add(new Card(HEARTS, ACE));
        notStraightFlush.add(new Card(CLUBS, TWO));
        notStraightFlush.add(new Card(CLUBS, SEVEN));
        notStraightFlush.add(new Card(CLUBS, FIVE));
        Cards hand2 = new Cards(notStraightFlush);
        assertFalse(PokerHandEvaluator.checkStraightFlush(hand2) > 0);

        Cards deck = new StandardDeck();
        assertTrue(PokerHandEvaluator.checkStraightFlush(deck) > 0);
    }

    @Test
    public void testFlush() throws CardException {
        List<Card> flush = new ArrayList<>();
        flush.add(new Card(SPADES, ACE));
        flush.add(new Card(SPADES, QUEEN));
        flush.add(new Card(SPADES, EIGHT));
        flush.add(new Card(SPADES, SEVEN));
        flush.add(new Card(SPADES, TWO));
        Cards hand = new Cards(flush);
        assertTrue(PokerHandEvaluator.checkFlush(hand) > 0);

        List<Card> notFlush = new ArrayList<>();
        notFlush.add(new Card(SPADES, ACE));
        notFlush.add(new Card(HEARTS, QUEEN));
        notFlush.add(new Card(SPADES, EIGHT));
        notFlush.add(new Card(CLUBS, SEVEN));
        notFlush.add(new Card(DIAMONDS, TWO));
        Cards hand2 = new Cards(notFlush);
        assertFalse(PokerHandEvaluator.checkFlush(hand2) > 0);

        Cards deck = new StandardDeck();
        assertTrue(PokerHandEvaluator.checkFlush(deck) > 0);
    }
}