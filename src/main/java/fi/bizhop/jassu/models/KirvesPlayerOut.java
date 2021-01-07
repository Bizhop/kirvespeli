package fi.bizhop.jassu.models;

import java.util.List;

public class KirvesPlayerOut {
    private String email;
    private int cardsInHand;
    private List<String> playedCards;

    public KirvesPlayerOut(KirvesPlayer player) {
        this.email = player.getUserEmail();
        this.cardsInHand = player.cardsInHand();
        this.playedCards = player.getPlayedCards().getCardsOut();
    }

    public String getEmail() {
        return email;
    }

    public int getCardsInHand() {
        return cardsInHand;
    }

    public List<String> getPlayedCards() {
        return playedCards;
    }
}
