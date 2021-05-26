package fi.bizhop.jassu.util;

import fi.bizhop.jassu.model.Card;
import fi.bizhop.jassu.model.Cards;
import fi.bizhop.jassu.model.poker.PokerHand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static fi.bizhop.jassu.model.Card.Suit.*;
import static fi.bizhop.jassu.model.poker.PokerHand.Type.*;

public class PokerHandEvaluator {
    public static PokerHand evaluate(Cards originalCards) {
        Cards cards = originalCards.copy();
        if(cards.size() != 5) {
            return new PokerHand(0, INVALID);
        }

        int straightFlush = checkStraightFlush(cards);
        if(straightFlush > 0) {
            return new PokerHand(straightFlush, STRAIGHT_FLUSH);
        }

        int four = checkFourOfAKind(cards);
        if(four > 0) {
            return new PokerHand(four, FOUR_OF_A_KIND);
        }

        int fullHouse = checkFullHouse(cards);
        if(fullHouse > 0) {
            return new PokerHand(fullHouse, FULL_HOUSE);
        }

        int flush = checkFlush(cards);
        if(flush > 0) {
            return new PokerHand(flush, FLUSH);
        }

        int straight = checkStraight(cards);
        if(straight > 0) {
            return new PokerHand(straight, STRAIGHT);
        }

        int three = checkThreeOfAKind(cards);
        if(three > 0) {
            return new PokerHand(three, THREE_OF_A_KIND);
        }

        int twoPairs = checkTwoPair(cards);
        if(twoPairs > 0) {
            return new PokerHand(twoPairs, TWO_PAIRS);
        }

        int pair = checkPair(cards);
        if(pair > 0) {
            return new PokerHand(pair, PAIR);
        }

        return new PokerHand(cards.highValue(), HIGH);
    }

    //return highest card of straight flush value or 0 if not present
    public static int checkStraightFlush(Cards cards) {
        final int MIN_HAND = 5;
        if(cards.size() < MIN_HAND) {
            return 0;
        }

        cards.sort();
        List<Integer> highs = new ArrayList<>();
        Cards spades = cards.filterBySuit(SPADES);
        Cards clubs = cards.filterBySuit(CLUBS);
        Cards diamonds = cards.filterBySuit(DIAMONDS);
        Cards hearts = cards.filterBySuit(HEARTS);
        highs.add(checkStraight(spades));
        highs.add(checkStraight(clubs));
        highs.add(checkStraight(diamonds));
        highs.add(checkStraight(hearts));
        highs.sort(Collections.reverseOrder());
        return highs.get(0);
    }

    //return four of a kind value or 0 if not present
    public static int checkFourOfAKind(Cards cards) {
        final int MIN_HAND = 4;
        if(cards.size() < MIN_HAND) {
            return 0;
        }

        cards.sort();
        List<Card> cardList = cards.getCards();
        for(int i = 0; i < cards.size() - MIN_HAND + 1; i++) {
            int rank = cardList.get(i).getRank().getValue();
            Cards rest = new Cards(cardList.subList(i+1, cards.size()));
            if(checkThreeOfAKind(rest) == rank) {
                return rank;
            }
        }
        return 0;
    }

    //return three of a kind value or 0 if not present
    public static int checkThreeOfAKind(Cards cards) {
        final int MIN_HAND = 3;
        if(cards.size() < MIN_HAND) {
            return 0;
        }

        cards.sort();
        List<Card> cardList = cards.getCards();
        for(int i = 0; i < cards.size() - MIN_HAND + 1; i++) {
            int rank = cardList.get(i).getRank().getValue();
            Cards rest = new Cards(cardList.subList(i+1, cards.size()));
            if(checkPair(rest) == rank) {
                return rank;
            }
        }
        return 0;
    }

    //return pair value or 0 if not present
    public static int checkPair(Cards cards) {
        final int MIN_HAND = 2;
        if(cards.size() < MIN_HAND) {
            return 0;
        }

        cards.sort(); //sort first to get highest pair
        List<Card> cardList = cards.getCards();
        for(int i = 0; i < cards.size() - MIN_HAND + 1; i++) {
            int rank = cardList.get(i).getRank().getValue();
            Cards rest = new Cards(cardList.subList(i+1, cards.size()));
            if(rest.containsRank(rank)) {
                return rank;
            }
        }
        return 0;
    }

    //return full house value (of three) or 0 if not present
    public static int checkFullHouse(Cards cards) {
        final int MIN_HAND = 5;
        if(cards.size() < MIN_HAND) {
            return 0;
        }
        cards.sort();

        int three = checkThreeOfAKind(cards);
        if(three > 0) {
            Cards other = cards.copy().removeFirstThree();
            if(checkPair(other) > 0) {
                return three;
            }
        }
        return 0;
    }

    //return highest flush card value or 0 if not present
    public static int checkFlush(Cards cards) {
        final int MIN_HAND = 5;
        if(cards.size() < MIN_HAND) {
            return 0;
        }

        cards.sort();
        List<Card> cardList = cards.getCards();
        for(int i=0; i < cards.size() - MIN_HAND + 1; i++) {
            Card.Suit suit = cardList.get(i).getSuit();
            Cards rest = new Cards(cardList.subList(i+1, cards.size()));
            if(rest.numOfSuit(suit) > 3) {
                return cardList.get(i).getRank().getValue();
            }
        }
        return 0;
    }

    //return highest straight card value or 0 if not present
    @SuppressWarnings("UnusedAssignment")
    public static int checkStraight(Cards cards) {
        final int MIN_HAND = 5;
        if(cards.size() < MIN_HAND) {
            return 0;
        }

        cards.sort();
        List<Card> cardList = cards.getCards();
        int possibleHighest = 0;
        for(int i=0; i < cards.size() - MIN_HAND + 1; i++) {
            int rank = cardList.get(i).getRank().getValue();
            Cards rest = new Cards(cardList.subList(i+1, cards.size()));
            if(rank == 14) {
                if(rest.containsRank(5) && rest.containsRank(4) && rest.containsRank(3) && rest.containsRank(2)) {
                    possibleHighest = 5;
                }
            }
            if(rank > 5) {
                if(rest.containsRank(rank - 1) && rest.containsRank(rank -2) && rest.containsRank(rank - 3) && rest.containsRank(rank - 4)) {
                    return rank;
                }
            }
            return possibleHighest;
        }
        return 0;
    }

    //return higher pair value or 0 if not present
    public static int checkTwoPair(Cards cards) {
        final int MIN_HAND = 4;
        if(cards.size() < MIN_HAND) {
            return 0;
        }

        cards.sort();
        int pair = checkPair(cards);
        if(pair > 0) {
            Cards other = cards.copy().removeFirstPair();
            if(checkPair(other) > 0) {
                return pair;
            }
        }
        return 0;
    }
}
