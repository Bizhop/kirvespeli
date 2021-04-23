package fi.bizhop.jassu.model.kirves;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.model.Card;
import fi.bizhop.jassu.model.Cards;
import fi.bizhop.jassu.model.StandardDeck;

public class Deck extends Cards {
    public Deck() throws CardException {
        this.cards.addAll(StandardDeck.getStandardDeck());
        this.cards.add(new Card(Card.Suit.JOKER, Card.Rank.BLACK));
        this.cards.add(new Card(Card.Suit.JOKER, Card.Rank.RED));
    }
}
