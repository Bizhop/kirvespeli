package fi.bizhop.jassu.util;

import fi.bizhop.jassu.models.Cards;
import fi.bizhop.jassu.models.PokerHand;

import static fi.bizhop.jassu.models.PokerHand.Type.*;

public class PokerHandEvaluator {
    public static PokerHand evaluate(Cards cards) {
        if(cards.size() != 5) {
            return new PokerHand(0, INVALID);
        }

        int straightFlush = cards.checkStraightFlush();
        if(straightFlush > 0) {
            return new PokerHand(straightFlush, STRAIGHT_FLUSH);
        }

        int four = cards.checkFourOfAKind();
        if(four > 0) {
            return new PokerHand(four, FOUR_OF_A_KIND);
        }

        int fullHouse = cards.checkFullHouse();
        if(fullHouse > 0) {
            return new PokerHand(fullHouse, FULL_HOUSE);
        }

        int flush = cards.checkFlush();
        if(flush > 0) {
            return new PokerHand(flush, FLUSH);
        }

        int straight = cards.checkStraight();
        if(straight > 0) {
            return new PokerHand(straight, STRAIGHT);
        }

        int three = cards.checkThreeOfAKind();
        if(three > 0) {
            return new PokerHand(three, THREE_OF_A_KIND);
        }

        int twoPairs = cards.checkTwoPair();
        if(twoPairs > 0) {
            return new PokerHand(twoPairs, TWO_PAIRS);
        }

        int pair = cards.checkPair();
        if(pair > 0) {
            return new PokerHand(pair, PAIR);
        }

        return new PokerHand(cards.highValue(), HIGH);
    }
}
