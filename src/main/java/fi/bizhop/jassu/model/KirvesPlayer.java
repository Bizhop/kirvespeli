package fi.bizhop.jassu.model;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KirvesPlayer implements Serializable {
    private static final long serialVersionUID = 1L;

    private final User user;
    private final Cards hand = new Cards();
    private final Cards invisibleCards  = new Cards();
    private Card extraCard;
    private final Cards playedCards = new Cards();
    private final List<Integer> roundsWon = new ArrayList<>();
    private final List<KirvesGame.Action> availableActions = new ArrayList<>();
    private KirvesPlayer next;
    private KirvesPlayer previous;
    private boolean declaredPlayer = false;
    private boolean inGame = true;

    /**
     * Create new player linking to previous and next players
     * @param user
     */
    public KirvesPlayer(User user, KirvesPlayer next, KirvesPlayer previous) {
        this.user = user;
        this.next = next;
        this.previous = previous;
        next.previous = this;
        previous.next = this;
    }

    /**
     * Create new player (first) linking only to self
     * @param user
     */
    public KirvesPlayer(User user) {
        this.user = user;
        this.next = this;
        this.previous = this;
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
        this.roundsWon.clear();
    }

    public void resetAvailableActions() {
        this.availableActions.clear();
    }

    public List<Integer> getRoundsWon() {
        return Collections.unmodifiableList(this.roundsWon);
    }

    public List<KirvesGame.Action> getAvailableActions() {
        return Collections.unmodifiableList(this.availableActions);
    }

    public void setAvailableActions(List<KirvesGame.Action> availableActions) {
        this.availableActions.clear();
        this.availableActions.addAll(availableActions);
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
       return next.inGame ? next : next.getNext();
    }

    public void setNext(KirvesPlayer next) {
        this.next = next;
    }

    public KirvesPlayer getPrevious() {
        return previous.inGame ? previous : previous.getPrevious();
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

    public boolean isDeclaredPlayer() {
        return declaredPlayer;
    }

    public void setDeclaredPlayer(boolean declaredPlayer) {
        this.declaredPlayer = declaredPlayer;
    }

    public void clearHand() {
        this.hand.clear();
    }

    public boolean isInGame() {
        return inGame;
    }

    public void activate() {
        this.inGame = true;
    }

    public void inactivate() {
        this.inGame = false;
    }
}
