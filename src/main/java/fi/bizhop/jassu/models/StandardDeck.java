package fi.bizhop.jassu.models;

public class StandardDeck extends Cards {
    public StandardDeck() {
        for(Card.Suit suit : Card.Suit.values()) {
            for(Card.Rank rank : Card.Rank.values()) {
                this.cards.add(new Card(suit, rank));
            }
        }
    }
}
