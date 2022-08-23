package fi.bizhop.jassu.model;

import fi.bizhop.jassu.exception.CardException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public class Card implements Comparable<Card> {
    private static final Logger LOG = LogManager.getLogger(Card.class);

    private final Suit SUIT;
    private final Rank RANK;

    private static final Map<String, Suit> SUITS_BY_ABBREVIATION =
            Arrays.stream(Suit.values()).collect(toMap(Suit::getAbbreviation, Function.identity()));
    private static final Map<String, Rank> RANKS_BY_ABBREVIATION =
            Arrays.stream(Rank.values()).collect(toMap(Rank::getAbbreviation, Function.identity()));


    public Suit getSuit() {
        return this.SUIT;
    }

    public Rank getRank() {
        return this.RANK;
    }

    public Card(Suit suit, Rank rank) throws CardException {
        if(suit == Suit.JOKER && rank.VALUE < 15) {
            throw new CardException("Joker must be rank 15 or 16");
        }
        this.SUIT = suit;
        this.RANK = rank;
    }

    public static Card fromAbbreviation(String abbreviation) {
        if(abbreviation == null || abbreviation.isEmpty()) return null;
        if(abbreviation.length() != 2) {
            LOG.warn("Unable to get card from abbreviation: {}", abbreviation);
            return null;
        }
        var suit = SUITS_BY_ABBREVIATION.get(abbreviation.substring(1));
        var rank = RANKS_BY_ABBREVIATION.get(abbreviation.substring(0, 1));
        if(suit == null || rank == null) {
            LOG.warn("Unable to get card from abbreviation: {}", abbreviation);
            return null;
        }
        try {
            return new Card(suit, rank);
        } catch (CardException e) {
            LOG.warn("Card exception caught when creating card from abbreviation", e);
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if(o instanceof Card) {
            var card = (Card) o;
            return this.SUIT == card.SUIT && this.RANK == card.RANK;
        }
        return false;
    }

    @Override
    //default sort is by rank desc
    public int compareTo(Card card) {
        return card.RANK.getValue() - this.RANK.getValue();
    }

    @Override
    public String toString() {
        return this.RANK.ABBREVIATION + this.SUIT.ABBREVIATION;
    }

    public enum Suit {
        CLUBS(1, "C"),
        DIAMONDS(2, "D"),
        HEARTS(3, "H"),
        SPADES(4, "S"),
        JOKER(5, "J");

        private final int VALUE;
        private final String ABBREVIATION;

        Suit(int value, String abbreviation) {
            this.VALUE = value;
            this.ABBREVIATION = abbreviation;
        }

        public static Suit fromAbbreviation(String abbreviation) {
            if(abbreviation == null || abbreviation.isEmpty()) return null;
            return SUITS_BY_ABBREVIATION.get(abbreviation);
        }

        public int getValue() {
            return this.VALUE;
        }
        public String getAbbreviation() { return this.ABBREVIATION; }
    }

    public enum Rank {
        TWO(2, "2"),
        THREE(3, "3"),
        FOUR(4, "4"),
        FIVE(5, "5"),
        SIX(6, "6"),
        SEVEN(7, "7"),
        EIGHT(8, "8"),
        NINE(9, "9"),
        TEN(10, "T"),
        JACK(11, "J"),
        QUEEN(12, "Q"),
        KING(13, "K"),
        ACE(14, "A"),

        //space 15-18 for Jacks in comparison

        //joker only
        BLACK(19, "B"),
        RED(20, "R");

        private final int VALUE;
        private final String ABBREVIATION;

        Rank(int value, String abbreviation) {
            this.VALUE = value;
            this.ABBREVIATION = abbreviation;
        }

        public int getValue() {
            return this.VALUE;
        }
        public String getAbbreviation() { return this.ABBREVIATION; }
    }
}