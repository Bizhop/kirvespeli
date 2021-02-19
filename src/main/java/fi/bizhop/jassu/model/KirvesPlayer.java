package fi.bizhop.jassu.model;

import fi.bizhop.jassu.exception.CardException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KirvesPlayer {
    private User player;
    private Cards hand;
    private Cards playedCards;
    private List<Integer> roundsWon;

    public KirvesPlayer(User player) {
        this.player = player;
        this.hand = new Cards();
        this.playedCards = new Cards();
        this.roundsWon = new ArrayList<>();
    }

    public String getUserEmail() {
        return this.player == null
                ? ""
                : this.player.getEmail();
    }

    public User getUser() {
        return this.player;
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

    public List<Integer> getRoundsWon() {
        return Collections.unmodifiableList(this.roundsWon);
    }
}
