package fi.bizhop.jassu.models;

import fi.bizhop.jassu.exception.CardException;

public class KirvesPlayer {
    private User player;
    private Cards hand;
    private Cards playedCards;

    public KirvesPlayer(User player) {
        this.player = player;
        this.hand = new Cards();
        this.playedCards = new Cards();
    }

    public String getUserEmail() {
        return player == null
                ? ""
                : player.getEmail();
    }

    public User getUser() {
        return player;
    }

    public int cardsInHand() {
        return this.hand.size();
    }

    public void playCard(int index) throws CardException {
        playedCards.add(this.hand.remove(index));
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
}
