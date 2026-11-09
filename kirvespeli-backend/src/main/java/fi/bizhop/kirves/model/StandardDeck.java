package fi.bizhop.kirves.model;

import fi.bizhop.kirves.exception.CardException;

import java.util.ArrayList;
import java.util.List;

import static fi.bizhop.kirves.model.Card.Rank.*;
import static fi.bizhop.kirves.model.Card.Suit.*;

public class StandardDeck extends Cards {
    public StandardDeck() throws CardException {
        this.cards.addAll(getStandardDeck());
    }

    public static List<Card> getStandardDeck() throws CardException {
        List<Card> newDeck = new ArrayList<>();
        for(Card.Suit suit : List.of(HEARTS, SPADES, DIAMONDS, CLUBS)) {
            for(Card.Rank rank : List.of(TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE)) {
                newDeck.add(new Card(suit, rank));
            }
        }
        return newDeck;
    }
}
