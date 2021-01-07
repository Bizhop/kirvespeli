package fi.bizhop.jassu.models;

public class KirvesPlayerOut {
    private String email;
    private int cardsInHand;

    public KirvesPlayerOut(KirvesPlayer player) {
        this.email = player.getUserEmail();
        this.cardsInHand = player.cardsInHand();
    }

    public String getEmail() {
        return email;
    }

    public int getCardsInHand() {
        return cardsInHand;
    }
}
