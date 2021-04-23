package fi.bizhop.jassu.model.poker;

import java.util.HashMap;
import java.util.Map;

public class PokerHand {
    public static final Map<Integer, String> cardNames;
    public int high;
    public Type type;

    static {
        cardNames = new HashMap<>();
        cardNames.put(0, "Invalid");
        cardNames.put(2, "Two");
        cardNames.put(3, "Three");
        cardNames.put(4, "Four");
        cardNames.put(5, "Five");
        cardNames.put(6, "Six");
        cardNames.put(7, "Seven");
        cardNames.put(8, "Eight");
        cardNames.put(9, "Nine");
        cardNames.put(10, "Ten");
        cardNames.put(11, "Jack");
        cardNames.put(12, "Queen");
        cardNames.put(13, "King");
        cardNames.put(14, "Ace");
    }

    public PokerHand(int high, Type type) {
        this.high = high;
        this.type = type;
    }

    public enum Type{
        INVALID("Virhe"),
        HIGH(""),
        PAIR("Pari"),
        TWO_PAIRS("Kaksi paria"),
        THREE_OF_A_KIND("Kolmoset"),
        STRAIGHT("Suora"),
        FLUSH("Väri"),
        FULL_HOUSE("Täyskäsi"),
        FOUR_OF_A_KIND("Neloset"),
        STRAIGHT_FLUSH("Värisuora");

        private final String text;
        Type(String text) {
            this.text = text;
        }
        public String getText() {
            return this.text;
        }
    }

    @Override
    public String toString() {
        return this.type.getText();
    }
}
