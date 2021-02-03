package fi.bizhop.jassu.models;

import java.util.List;

public class KirvesPlayerOut {
    private String email;
    private int cardsInHand;
    private List<String> playedCards;
    private List<Integer> roundsWon;

    public KirvesPlayerOut(KirvesPlayer player) {
        this.email = player.getUserEmail();
        this.cardsInHand = player.cardsInHand();
        this.playedCards = player.getPlayedCards().getCardsOut();
        this.roundsWon = player.getRoundsWon();
    }

    public String getEmail() {
        return this.email;
    }

    public int getCardsInHand() {
        return this.cardsInHand;
    }

    public List<String> getPlayedCards() {
        return this.playedCards;
    }

    public List<Integer> getRoundsWon() {
        return this.roundsWon;
    }
}
