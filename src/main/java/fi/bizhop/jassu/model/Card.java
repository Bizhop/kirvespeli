package fi.bizhop.jassu.model;

import fi.bizhop.jassu.exception.CardException;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public class Card implements Comparable<Card> {
    private final Suit SUIT;
    private final Rank RANK;

    private static final Map<String, Suit> SUITS_BY_ABBR =
            Arrays.stream(Suit.values()).collect(toMap(Suit::getAbbr, Function.identity()));
    private static final Map<String, Rank> RANKS_BY_ABBR =
            Arrays.stream(Rank.values()).collect(toMap(Rank::getAbbr, Function.identity()));;


    public Suit getSuit() {
        return SUIT;
    }

    public Rank getRank() {
        return RANK;
    }

    public Card(Suit suit, Rank rank) throws CardException {
        if(suit == Suit.JOKER && rank.value < 15) {
            throw new CardException("Joker must be rank 15 or 16");
        }
        this.SUIT = suit;
        this.RANK = rank;
    }

    public static Card fromAbbr(String abbr) throws CardException {
        if(abbr == null || abbr.isEmpty()) return null;
        if(abbr.length() != 2) throw new CardException("Invalid abbr");
        Suit suit = SUITS_BY_ABBR.get(abbr.substring(1));
        Rank rank = RANKS_BY_ABBR.get(abbr.substring(0, 1));
        if(suit == null || rank == null) throw new CardException("Invalid abbr");
        return new Card(suit, rank);
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if(o instanceof Card) {
            Card card = (Card) o;
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
        return this.RANK.abbr + this.SUIT.abbr;
    }

    public enum Suit {
        CLUBS(1, "C"),
        DIAMONDS(2, "D"),
        HEARTS(3, "H"),
        SPADES(4, "S"),
        JOKER(5, "J");

        private final int value;
        private final String abbr;

        Suit(int value, String abbr) {
            this.value = value;
            this.abbr = abbr;
        }

        public static Suit fromAbbr(String abbr) {
            if(abbr == null || abbr.isEmpty()) return null;
            return SUITS_BY_ABBR.get(abbr);
        }

        public int getValue() {
            return this.value;
        }
        public String getAbbr() { return this.abbr; }
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

        private final int value;
        private final String abbr;

        Rank(int value, String abbr) {
            this.value = value;
            this.abbr = abbr;
        }

        public int getValue() {
            return value;
        }
        public String getAbbr() { return this.abbr; }
    }
}