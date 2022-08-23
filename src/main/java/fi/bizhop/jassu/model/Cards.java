package fi.bizhop.jassu.model;

import fi.bizhop.jassu.exception.CardException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Cards {
    private static final Logger LOG = LogManager.getLogger(Cards.class);

    protected final List<Card> cards;

    public Cards() {
        this.cards = new ArrayList<>();
    }

    public Cards(List<Card> cards) {
        this.cards = cards;
    }

    public static Cards fromAbbreviations(List<String> abbreviations) {
        if(abbreviations == null) return new Cards();
        List<Card> cards = abbreviations.stream()
                .map(Card::fromAbbreviation)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new Cards(cards);
    }

    //default sort is by rank
    public void sort() {
        Collections.sort(this.cards);
    }

    public Cards shuffle() {
        Collections.shuffle(this.cards);
        return this;
    }

    public Cards deal(int quantity) throws CardException {
        if(quantity > this.cards.size()) {
            throw new CardException(String.format("Not enough cards: wanted %d, has %d", quantity, this.cards.size()));
        }
        List<Card> given = new ArrayList<>();
        for(int i=0; i < quantity; i++) {
            given.add(this.cards.remove(0));
        }
        return new Cards(given);
    }

    public Card remove(int index) throws CardException {
        if(index < 0 || index > this.cards.size() - 1) {
            throw new CardException("Invalid card index");
        }
        return this.cards.remove(index);
    }

    public Card get(int index) throws CardException{
        if(index < 0 || index > this.cards.size() - 1) {
            throw new CardException("Invalid card index");
        }
        return this.cards.get(index);
    }

    @Override
    public String toString() {
        return IntStream.range(0, this.cards.size())
                .mapToObj(i -> String.format("%d:%s", i, this.cards.get(i).toString()))
                .collect(Collectors.joining(", "));
    }

    public int size() {
        return this.cards.size();
    }

    public List<String> getCardsOut() {
        return this.cards.stream()
                .map(Card::toString)
                .collect(Collectors.toList());
    }

    public void clear() {
        this.cards.clear();
    }

    public Card last() {
        return this.cards.isEmpty() ? null : this.cards.get(this.cards.size() - 1);
    }

    public void add(Card newCard) {
        this.cards.add(newCard);
    }

    public void add(Cards newCards) {
        this.cards.addAll(newCards.cards);
    }

    //remove specific card from deck
    public Card removeCard(Card card) throws CardException {
        if(this.cards.remove(card)) {
            return card;
        }
        else throw new CardException(String.format("Card %s not found in deck", card));
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Cards)) return false;
        var other = (Cards)o;
        if(this.size() != other.size()) return false;
        for(int i=0; i < this.size(); i++) {
           if(!this.cards.get(i).equals(other.cards.get(i))) return false;
        }
        return true;
    }

    public boolean hasNoTrumpCard(Card.Suit trump) {
        return this.cards.stream().noneMatch(card -> card.getRank() == Card.Rank.JACK || List.of(trump, Card.Suit.JOKER).contains(card.getSuit()));
    }
}