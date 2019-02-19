package fi.bizhop.jassu.util;

import fi.bizhop.jassu.models.*;
import fi.bizhop.jassu.service.PokerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static fi.bizhop.jassu.models.Card.Rank.*;
import static fi.bizhop.jassu.models.Card.Suit.*;
import static fi.bizhop.jassu.models.PokerHand.Type.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PokerServiceTest {
    @Autowired
    PokerService poker;

    //don't run this with builds
    //@Test
    public void testHand() {
        getHand(HIGH);
        getHand(PAIR);
        getHand(TWO_PAIRS);
        getHand(THREE_OF_A_KIND);
        getHand(STRAIGHT);
        getHand(FLUSH);
        getHand(FULL_HOUSE);
        getHand(FOUR_OF_A_KIND);
        getHand(STRAIGHT_FLUSH);
    }

    private void getHand(PokerHand.Type type) {
        long time = System.currentTimeMillis();
        int reps = 0;
        boolean run = true;
        while(run) {
            reps++;
            PokerGame game = poker.newGame().deal();
            Cards hand = game.getHand();
            PokerHand ev = hand.evaluate();
            if(ev.type == type) {
                System.out.println(ev);
                System.out.println(hand);
                System.out.println(String.format("Reps: %d, time: %dms", reps, System.currentTimeMillis() - time));
                run = false;
            }
        }
    }

    @Test
    public void testPair() {
        List<Card> pair = new ArrayList<>();
        pair.add(new Card(SPADES, ACE));
        pair.add(new Card(HEARTS, ACE));
        Cards hand = new Cards(pair);
        assertTrue(hand.checkPair() > 0);

        List<Card> notPair = new ArrayList<>();
        notPair.add(new Card(SPADES, ACE));
        notPair.add(new Card(SPADES, TWO));
        Cards hand2 = new Cards(notPair);
        assertFalse(hand2.checkPair() > 0);

        Cards deck = new StandardDeck();
        assertTrue(deck.checkPair() > 0);
    }

    @Test
    public void testThree() {
        List<Card> three = new ArrayList<>();
        three.add(new Card(SPADES, ACE));
        three.add(new Card(HEARTS, ACE));
        three.add(new Card(CLUBS, ACE));
        Cards hand = new Cards(three);
        assertTrue(hand.checkThreeOfAKind() > 0);

        List<Card> notThree = new ArrayList<>();
        notThree.add(new Card(SPADES, ACE));
        notThree.add(new Card(HEARTS, ACE));
        notThree.add(new Card(CLUBS, TWO));
        Cards hand2 = new Cards(notThree);
        assertFalse(hand2.checkThreeOfAKind() > 0);

        Cards deck = new StandardDeck();
        assertTrue(deck.checkThreeOfAKind() > 0);
    }

    @Test
    public void testTwoPair() {
        List<Card> twoPair = new ArrayList<>();
        twoPair.add(new Card(SPADES, ACE));
        twoPair.add(new Card(HEARTS, ACE));
        twoPair.add(new Card(SPADES, KING));
        twoPair.add(new Card(HEARTS, KING));
        Cards hand = new Cards(twoPair);
        assertTrue(hand.checkTwoPair() > 0);

        List<Card> notTwoPair = new ArrayList<>();
        notTwoPair.add(new Card(SPADES, ACE));
        notTwoPair.add(new Card(HEARTS, ACE));
        notTwoPair.add(new Card(SPADES, KING));
        notTwoPair.add(new Card(HEARTS, SEVEN));
        Cards hand2 = new Cards(notTwoPair);
        assertFalse(hand2.checkTwoPair() > 0);

        Cards deck = new StandardDeck();
        assertTrue(deck.checkTwoPair() > 0);
    }

    @Test
    public void testStraight() {
        List<Card> straight = new ArrayList<>();
        straight.add(new Card(HEARTS, KING));
        straight.add(new Card(SPADES, QUEEN));
        straight.add(new Card(HEARTS, JACK));
        straight.add(new Card(CLUBS, TEN));
        straight.add(new Card(SPADES, NINE));
        Cards hand = new Cards(straight);
        assertTrue(hand.checkStraight() > 0);

        List<Card> specialStraight = new ArrayList<>();
        specialStraight.add(new Card(SPADES, ACE));
        specialStraight.add(new Card(HEARTS, TWO));
        specialStraight.add(new Card(SPADES, THREE));
        specialStraight.add(new Card(HEARTS, FOUR));
        specialStraight.add(new Card(CLUBS, FIVE));
        Cards hand2 = new Cards(specialStraight);
        assertTrue(hand2.checkStraight() > 0);

        List<Card> notStraight = new ArrayList<>();
        notStraight.add(new Card(HEARTS, KING));
        notStraight.add(new Card(SPADES, QUEEN));
        notStraight.add(new Card(HEARTS, JACK));
        notStraight.add(new Card(CLUBS, TEN));
        notStraight.add(new Card(SPADES, SIX));
        Cards hand3 = new Cards(notStraight);
        assertFalse(hand3.checkStraight() > 0);

        Cards deck = new StandardDeck();
        assertTrue(deck.checkStraight() > 0);
    }

    @Test
    public void testFullHouse() {
        List<Card> fullHouse = new ArrayList<>();
        fullHouse.add(new Card(SPADES, ACE));
        fullHouse.add(new Card(HEARTS, ACE));
        fullHouse.add(new Card(CLUBS, ACE));
        fullHouse.add(new Card(DIAMONDS, KING));
        fullHouse.add(new Card(HEARTS, KING));
        Cards hand = new Cards(fullHouse);
        assertTrue(hand.checkFullHouse() > 0);

        List<Card> notFullHouse = new ArrayList<>();
        notFullHouse.add(new Card(SPADES, ACE));
        notFullHouse.add(new Card(HEARTS, ACE));
        notFullHouse.add(new Card(CLUBS, TWO));
        notFullHouse.add(new Card(CLUBS, SEVEN));
        notFullHouse.add(new Card(CLUBS, FIVE));
        Cards hand2 = new Cards(notFullHouse);
        assertFalse(hand2.checkFullHouse() > 0);

        Cards deck = new StandardDeck();
        assertTrue(deck.checkFullHouse() > 0);
    }

    @Test
    public void testFour() {
        List<Card> four = new ArrayList<>();
        four.add(new Card(SPADES, ACE));
        four.add(new Card(HEARTS, ACE));
        four.add(new Card(CLUBS, ACE));
        four.add(new Card(DIAMONDS, ACE));
        Cards hand = new Cards(four);
        assertTrue(hand.checkFourOfAKind() > 0);

        List<Card> notFour = new ArrayList<>();
        notFour.add(new Card(SPADES, ACE));
        notFour.add(new Card(HEARTS, ACE));
        notFour.add(new Card(CLUBS, TWO));
        Cards hand2 = new Cards(notFour);
        assertFalse(hand2.checkFourOfAKind() > 0);

        Cards deck = new StandardDeck();
        assertTrue(deck.checkFourOfAKind() > 0);
    }

    @Test
    public void testStraightFlush() {
        List<Card> straightFlush = new ArrayList<>();
        straightFlush.add(new Card(SPADES, ACE));
        straightFlush.add(new Card(SPADES, KING));
        straightFlush.add(new Card(SPADES, QUEEN));
        straightFlush.add(new Card(SPADES, JACK));
        straightFlush.add(new Card(SPADES, TEN));
        Cards hand = new Cards(straightFlush);
        assertTrue(hand.checkStraightFlush() > 0);

        List<Card> notStraightFlush = new ArrayList<>();
        notStraightFlush.add(new Card(SPADES, ACE));
        notStraightFlush.add(new Card(HEARTS, ACE));
        notStraightFlush.add(new Card(CLUBS, TWO));
        notStraightFlush.add(new Card(CLUBS, SEVEN));
        notStraightFlush.add(new Card(CLUBS, FIVE));
        Cards hand2 = new Cards(notStraightFlush);
        assertFalse(hand2.checkStraightFlush() > 0);

        Cards deck = new StandardDeck();
        assertTrue(deck.checkStraightFlush() > 0);
    }
}