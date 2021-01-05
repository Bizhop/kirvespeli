package fi.bizhop.jassu.models;

import fi.bizhop.jassu.exception.CardException;

public class KirvesDeck extends Cards {
    public KirvesDeck() throws CardException {
        this.cards.addAll(StandardDeck.getStandardDeck());
        this.cards.add(new Card(Card.Suit.JOKER, Card.Rank.BLACK));
        this.cards.add(new Card(Card.Suit.JOKER, Card.Rank.RED));
    }
}
