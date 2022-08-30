package fi.bizhop.kirves.model.kirves;

import fi.bizhop.kirves.exception.CardException;
import fi.bizhop.kirves.model.Card;
import fi.bizhop.kirves.model.Cards;
import fi.bizhop.kirves.model.StandardDeck;

public class Deck extends Cards {
    public Deck() throws CardException {
        this.cards.addAll(StandardDeck.getStandardDeck());
        this.cards.add(new Card(Card.Suit.JOKER, Card.Rank.BLACK));
        this.cards.add(new Card(Card.Suit.JOKER, Card.Rank.RED));
    }
}
