package fi.bizhop.jassu.model;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KirvesPlayer {
    private final User user;
    private final Cards hand;
    private final Cards invisibleCards;
    private Card extraCard;
    private final Cards playedCards;
    private List<Integer> roundsWon;
    private List<KirvesGame.Action> availableActions;
    private KirvesPlayer next;
    private KirvesPlayer previous;

    public KirvesPlayer(User user) {
        this.user = user;
        this.hand = new Cards();
        this.invisibleCards = new Cards();
        this.playedCards = new Cards();
        this.roundsWon = new ArrayList<>();
        this.availableActions = new ArrayList<>();
    }

    public String getUserEmail() {
        return this.user == null
                ? ""
                : this.user.getEmail();
    }

    public String getUserNickname() {
        return this.user == null
                ? ""
                : this.user.getNickname();
    }

    public User getUser() {
        return this.user;
    }

    public int cardsInHand() {
        return this.hand.size();
    }

    public void playCard(int index) throws CardException {
        this.playedCards.add(this.hand.remove(index));
    }

    public Cards getPlayedCards() {
        return this.playedCards;
    }

    public void addCards(Cards newCards) {
        this.hand.add(newCards);
    }

    public Cards getHand() {
        return this.hand;
    }

    public void addRoundWon() {
        this.roundsWon.add(this.playedCards.size() - 1);
    }

    public void resetWonRounds() {
        this.roundsWon = new ArrayList<>();
    }

    public void resetAvailableActions() {
        this.availableActions = new ArrayList<>();
    }

    public List<Integer> getRoundsWon() {
        return Collections.unmodifiableList(this.roundsWon);
    }

    public List<KirvesGame.Action> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List<KirvesGame.Action> availableActions) {
        this.availableActions = availableActions;
    }

    public Card getExtraCard() {
        return extraCard;
    }

    public Card getLastPlayedCard() {
        return this.playedCards.last();
    }

    public boolean hasInvisibleCards() {
        return this.invisibleCards.size() > 0;
    }

    public void hideCards(int numberOfCards) throws CardException {
        this.invisibleCards.add(this.hand.deal(numberOfCards));
    }

    public void moveInvisibleCardsToHand() {
        this.hand.add(this.invisibleCards);
        this.invisibleCards.clear();
    }

    public void setExtraCard(Card extraCard) {
        this.extraCard = extraCard;
    }

    public void discard(int index) throws KirvesGameException, CardException {
        if(extraCard == null) {
            throw new KirvesGameException("Cannot discard without extra card");
        }
        this.hand.remove(index);
        this.hand.add(this.extraCard);
        this.extraCard = null;
    }

    public KirvesPlayer getNext() {
        return next;
    }

    public void setNext(KirvesPlayer next) {
        this.next = next;
    }

    public KirvesPlayer getPrevious() {
        return previous;
    }

    public void setPrevious(KirvesPlayer previous) {
        this.previous = previous;
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof KirvesPlayer) {
            return this.user.equals(((KirvesPlayer) other).user);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return this.getUserEmail();
    }
}
