package fi.bizhop.jassu.models;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.util.PokerHandEvaluator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static fi.bizhop.jassu.models.Card.Suit.*;

public class Cards {
    protected List<Card> cards = new ArrayList<>();

    public Cards() {}

    public Cards(List<Card> cards) {
        this.cards = cards;
    }

    //default sort is by rank
    public Cards sort() {
        Collections.sort(cards);
        return this;
    }

    public void sort(Comparator<Card> c) {
        Collections.sort(cards, c);
    }

    public Cards shuffle() {
        Collections.shuffle(cards);
        return this;
    }

    public Cards give(int quantity) throws CardException {
        if(quantity > cards.size()) {
            throw new CardException(String.format("Not enough cards: wanted %d, has %d", quantity, cards.size()));
        }
        List<Card> given = new ArrayList<>();
        for(int i=0; i < quantity; i++) {
            given.add(this.cards.remove(0));
        }
        return new Cards(given);
    }

    @Override
    public String toString() {
        return IntStream.range(0, cards.size())
                .mapToObj(i -> String.format("%d:%s", i, cards.get(i).toString()))
                .collect(Collectors.joining(", "));
    }

    public int size() {
        return this.cards.size();
    }

    public int checkStraightFlush() {
        final int MINHAND = 5;
        if(this.cards.size() < MINHAND) {
            return 0;
        }

        this.sort();
        List<Integer> highs = new ArrayList<>();
        Cards spades = this.filterBySuit(SPADES);
        Cards clubs = this.filterBySuit(CLUBS);
        Cards diamonds = this.filterBySuit(DIAMONDS);
        Cards hearts = this.filterBySuit(HEARTS);
        highs.add(spades.checkStraight());
        highs.add(clubs.checkStraight());
        highs.add(diamonds.checkStraight());
        highs.add(hearts.checkStraight());
        highs.sort(Collections.reverseOrder());
        return highs.get(0);
    }

    public int checkFourOfAKind() {
        final int MINHAND = 4;
        if(this.cards.size() < MINHAND) {
            return 0;
        }

        this.sort();
        for(int i = 0; i < this.cards.size() - MINHAND + 1; i++) {
            int rank = this.cards.get(i).getRank().getValue();
            Cards rest = new Cards(this.cards.subList(i+1, this.cards.size()));
            if(rest.checkThreeOfAKind() == rank) {
                return rank;
            }
        }
        return 0;
    }

    public int checkFullHouse() {
        final int MINHAND = 5;
        if(this.cards.size() < MINHAND) {
            return 0;
        }
        this.sort();
        int three = this.checkThreeOfAKind();
        if(three > 0) {
            Cards other = this.copy().removeFirstThree();
            if(other.checkPair() > 0) {
                return three;
            }
        }
        return 0;
    }

    //return three of a kind value or 0 if not present
    public int checkThreeOfAKind() {
        final int MINHAND = 3;
        if(this.cards.size() < MINHAND) {
            return 0;
        }
        this.sort();
        for(int i = 0; i < this.cards.size() - MINHAND + 1; i++) {
            int rank = this.cards.get(i).getRank().getValue();
            Cards rest = new Cards(this.cards.subList(i+1, this.cards.size()));
            if(rest.checkPair() == rank) {
                return rank;
            }
        }
        return 0;
    }

    //return highest flush card value or 0 if not present
    public int checkFlush() {
        final int MINHAND = 5;
        if(this.cards.size() < MINHAND) {
            return 0;
        }
        this.sort();
        for(int i=0; i < this.cards.size() - MINHAND + 1; i++) {
            int suit = this.cards.get(i).getSuit().getValue();
            Cards rest = new Cards(this.cards.subList(i+1, this.cards.size()));
            if(rest.numOfSuit(suit) > 3) {
                return this.cards.get(i).getRank().getValue();
            }
        }
        return 0;
    }

    //return highest straight card value or 0 if not present
    public int checkStraight() {
        final int MINHAND = 5;
        if(this.cards.size() < MINHAND) {
            return 0;
        }
        this.sort();
        int possibleHighest = 0;
        for(int i=0; i < this.cards.size() - MINHAND + 1; i++) {
            int rank = this.cards.get(i).getRank().getValue();
            Cards rest = new Cards(this.cards.subList(i+1, this.cards.size()));
            if(rank == 14) {
                if(rest.containsRank(5) && rest.containsRank(4) && rest.containsRank(3) && rest.containsRank(2)) {
                    possibleHighest = 5;
                }
            }
            if(rank > 5) {
                if(rest.containsRank(rank - 1) && rest.containsRank(rank -2) && rest.containsRank(rank - 3) && rest.containsRank(rank - 4)) {
                    return rank;
                }
            }
            return possibleHighest;
        }
        return 0;
    }

    //return higher pair value or 0 if not present
    public int checkTwoPair() {
        final int MINHAND = 4;
        if(this.cards.size() < MINHAND) {
            return 0;
        }
        this.sort();
        int pair = this.checkPair();
        if(pair > 0) {
            Cards other = this.copy().removeFirstPair();
            if(other.checkPair() > 0) {
                return pair;
            }
        }
        return 0;
    }

    //return pair value or 0 if not present
    public int checkPair() {
        final int MINHAND = 2;
        if(this.cards.size() < MINHAND) {
            return 0;
        }
        this.sort(); //sort first to get highest pair
        for(int i = 0; i < this.cards.size() - MINHAND + 1; i++) {
            int rank = this.cards.get(i).getRank().getValue();
            Cards rest = new Cards(this.cards.subList(i+1, this.cards.size()));
            if(rest.containsRank(rank)) {
                return rank;
            }
        }
        return 0;
    }

    public int highValue() {
        final int MINHAND = 1;
        if(this.cards.size() < MINHAND) {
            return 0;
        }
        this.sort();
        return cards.get(0).getRank().getValue();
    }

    public PokerHand evaluate() {
        return PokerHandEvaluator.evaluate(this);
    }

    public void hold(List<Integer> params, Cards deck) throws CardException {
        //sanity check
        if(params == null || params.size() > cards.size()) {
            return;
        } else if (params.isEmpty()) {
            this.cards = deck.give(5).cards;
            return;
        }
        params.sort(Integer::compareTo);
        if(params.get(0) < 0 || params.get(params.size() -1) > cards.size() - 1) {
            return;
        }

        for(int i=0; i < cards.size(); i++) {
            if(!params.contains(i)) {
                this.cards.set(i, deck.give(1).cards.get(0));
            }
        }
    }

    private void add(Cards cardsToAdd) {
        this.cards.addAll(cardsToAdd.cards);
    }

    public Cards copy() {
        return new Cards(new ArrayList<>(this.cards));
    }

    private Cards removeFirstPair() {
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

    private Cards removeFirstThree() {
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

    private boolean containsRank(int rank) {
        for(Card card : this.cards) {
            if(card.getRank().getValue() == rank) {
                return true;
            }
        }
        return false;
    }

    private long numOfSuit(int suit) {
        return cards.stream()
                .filter(c -> c.getSuit().getValue() == suit)
                .count();
    }

    private Cards filterBySuit(Card.Suit suit) {
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
        cards = new ArrayList<>();
    }

    public Card first() {
        return cards.get(0);
    }

    public void add(Card doubleCard) {
        cards.add(doubleCard);
    }
}