package fi.bizhop.jassu.models;

public class Card implements Comparable<Card> {
    private final Suit suit;
    private final Rank rank;

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    @Override
    //default sort is by rank desc
    public int compareTo(Card card) {
        return card.rank.getValue() - this.rank.getValue();
    }

    @Override
    public String toString() {
        return this.rank.abbr + this.suit.abbr;
    }

    public enum Suit {
        CLUBS(1, "C"),
        DIAMONDS(2, "D"),
        HEARTS(3, "H"),
        SPADES(4, "S");

        private final int value;
        private final String abbr;

        Suit(int value, String abbr) {
            this.value = value;
            this.abbr = abbr;
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
        ACE(14, "A");

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