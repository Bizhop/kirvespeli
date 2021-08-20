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

    public int highValue() {
        final int MIN_HAND = 1;
        if(this.cards.size() < MIN_HAND) {
            return 0;
        }
        this.sort();
        return this.cards.get(0).getRank().getValue();
    }

    //TODO: consider moving this logic to some poker specific class
    public void hold(List<Integer> paramsList, Cards deck) throws CardException {
        //sanity check
        if(paramsList == null || paramsList.size() > this.cards.size()) {
            return;
        } else if (paramsList.isEmpty()) {
            this.cards.clear();
            this.cards.addAll(deck.deal(5).cards);
            return;
        }
        var params = new ArrayList<>(paramsList);
        params.sort(Integer::compareTo);
        if(params.get(0) < 0 || params.get(params.size() -1) > this.cards.size() - 1) {
            return;
        }

        for(int i = 0; i < this.cards.size(); i++) {
            if(!params.contains(i)) {
                this.cards.set(i, deck.deal(1).cards.get(0));
            }
        }
    }

    public Cards copy() {
        return new Cards(new ArrayList<>(this.cards));
    }

    public Cards removeFirstPair() {
        if(this.cards.size() > 1) {
            for(int i=0; i < this.cards.size() - 1; i++) {
                Card current = this.cards.get(i);
                Card next = this.cards.get(i + 1);
                if(current.getRank().getValue() == next.getRank().getValue()) {
                    this.cards.remove(current);
                    this.cards.remove(next);
                    return this;
                }
            }
        }
        return this;
    }

    public Cards removeFirstThree() {
        if(this.cards.size() > 2) {
            for(int i=0; i < this.cards.size() - 2; i++) {
                Card current = this.cards.get(i);
                Card next = this.cards.get(i + 1);
                Card next2 = this.cards.get(i + 2);
                if(current.getRank().getValue() == next.getRank().getValue() && current.getRank().getValue() == next2.getRank().getValue()) {
                    this.cards.remove(current);
                    this.cards.remove(next);
                    this.cards.remove(next2);
                    return this;
                }
            }
        }
        return this;
    }

    public boolean containsRank(int rank) {
        for(Card card : this.cards) {
            if(card.getRank().getValue() == rank) {
                return true;
            }
        }
        return false;
    }

    public long numOfSuit(Card.Suit suit) {
        return this.cards.stream()
                .filter(c -> c.getSuit() == suit)
                .count();
    }

    public Cards filterBySuit(Card.Suit suit) {
        List<Card> bySuit = this.cards.stream()
                .filter(c -> c.getSuit() == suit)
                .collect(Collectors.toList());
        return new Cards(bySuit);
    }

    public List<String> getCardsOut() {
        return this.cards.stream()
                .map(Card::toString)
                .collect(Collectors.toList());
    }

    public void clear() {
        this.cards.clear();
    }

    public Card first() {
        return this.cards.isEmpty() ? null : this.cards.get(0);
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

    //returns a copy of cards
    public List<Card> getCards() {
        return new ArrayList<>(this.cards);
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
        Cards other = (Cards)o;
        if(this.size() != other.size()) return false;
        for(int i=0; i < this.size(); i++) {
           if(!this.cards.get(i).equals(other.cards.get(i))) return false;
        }
        return true;
    }

    public boolean hasNoTrumpCard(Card.Suit trump) {
        return this.cards.stream().noneMatch(card -> card.getRank() == Card.Rank.JACK || List.of(trump, Card.Suit.JOKER).contains(card.getSuit()));
    }

    public Cards giveCards(Cards hand) throws CardException {
        if(hand == null) return new Cards();
        if(this.cards.containsAll(hand.cards)) {
            this.cards.removeAll(hand.getCards());
            return hand;
        } else {
            throw new CardException("All wanted cards don't exist");
        }
    }
}