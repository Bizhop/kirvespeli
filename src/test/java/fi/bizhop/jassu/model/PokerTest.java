package fi.bizhop.jassu.model;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.util.PokerHandEvaluator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PokerTest {
    @Test
    public void testPair() throws CardException {
        Cards pair = Cards.fromAbbreviations(List.of("7S", "7H"));
        assertEquals(7, PokerHandEvaluator.checkPair(pair));

        Cards notPair = Cards.fromAbbreviations(List.of("AS", "2S"));
        assertEquals(0, PokerHandEvaluator.checkPair(notPair));

        Cards deck = new StandardDeck();
        assertEquals(14, PokerHandEvaluator.checkPair(deck));
    }

    @Test
    public void testThree() throws CardException {
        Cards three = Cards.fromAbbreviations(List.of("9S", "9H", "9C"));
        assertEquals(9, PokerHandEvaluator.checkThreeOfAKind(three));

        Cards notThree = Cards.fromAbbreviations(List.of("AS", "AH", "2C"));
        assertEquals(0, PokerHandEvaluator.checkThreeOfAKind(notThree));

        Cards deck = new StandardDeck();
        assertEquals(14, PokerHandEvaluator.checkThreeOfAKind(deck));
    }

    @Test
    public void testTwoPair() throws CardException {
        Cards twoPair = Cards.fromAbbreviations(List.of("TS", "TH", "5S", "5H"));
        assertEquals(10, PokerHandEvaluator.checkTwoPair(twoPair));

        Cards notTwoPair = Cards.fromAbbreviations(List.of("AS", "AH", "KS", "7H"));
        assertEquals(0, PokerHandEvaluator.checkTwoPair(notTwoPair));

        Cards deck = new StandardDeck();
        assertEquals(14, PokerHandEvaluator.checkTwoPair(deck));
    }

    @Test
    public void testStraight() throws CardException {
        Cards straight = Cards.fromAbbreviations(List.of("KH", "QS", "JH", "TC", "9S"));
        assertEquals(13, PokerHandEvaluator.checkStraight(straight));

        Cards specialStraight = Cards.fromAbbreviations(List.of("AH", "2S", "3H", "4C", "5S"));
        assertEquals(5, PokerHandEvaluator.checkStraight(specialStraight));

        Cards notStraight = Cards.fromAbbreviations(List.of("KH", "QS", "JH", "TC", "6S"));
        assertEquals(0, PokerHandEvaluator.checkStraight(notStraight));

        Cards deck = new StandardDeck();
        assertEquals(14, PokerHandEvaluator.checkStraight(deck));
    }

    @Test
    public void testFullHouse() throws CardException {
        Cards fullHouse = Cards.fromAbbreviations(List.of("8S", "8H", "8C", "KD", "KH"));
        assertEquals(8, PokerHandEvaluator.checkFullHouse(fullHouse));

        Cards notFullHouse = Cards.fromAbbreviations(List.of("AS", "AH", "2C", "7C", "5C"));
        assertEquals(0, PokerHandEvaluator.checkFullHouse(notFullHouse));

        Cards deck = new StandardDeck();
        assertEquals(14, PokerHandEvaluator.checkFullHouse(deck));
    }

    @Test
    public void testFour() throws CardException {
        Cards four = Cards.fromAbbreviations(List.of("6S", "6H", "6C", "6D"));
        assertEquals(6, PokerHandEvaluator.checkFourOfAKind(four));

        Cards notFour = Cards.fromAbbreviations(List.of("AS", "AH", "AC", "2D"));
        assertEquals(0, PokerHandEvaluator.checkFourOfAKind(notFour));

        Cards deck = new StandardDeck();
        assertEquals(14, PokerHandEvaluator.checkFourOfAKind(deck));
    }

    @Test
    public void testStraightFlush() throws CardException {
        Cards straightFlush = Cards.fromAbbreviations(List.of("9S", "8S", "QS", "JS", "TS"));
        assertEquals(12, PokerHandEvaluator.checkStraightFlush(straightFlush));

        Cards notStraightFlush = Cards.fromAbbreviations(List.of("AS", "AH", "AC", "2D", "6S"));
        assertEquals(0, PokerHandEvaluator.checkStraightFlush(notStraightFlush));

        Cards deck = new StandardDeck();
        assertEquals(14, PokerHandEvaluator.checkStraightFlush(deck));
    }

    @Test
    public void testFlush() throws CardException {
        Cards flush = Cards.fromAbbreviations(List.of("5S", "JS", "8S", "7S", "2S"));
        assertEquals(11, PokerHandEvaluator.checkFlush(flush));

        Cards notFlush = Cards.fromAbbreviations(List.of("AS", "QH", "8S", "7C", "2D"));
        assertEquals(0, PokerHandEvaluator.checkFlush(notFlush));

        Cards deck = new StandardDeck();
        assertEquals(14, PokerHandEvaluator.checkFlush(deck));
    }
}