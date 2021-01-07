package fi.bizhop.jassu.models;

public class KirvesPlayer {
    private User player;
    private Cards hand;

    public KirvesPlayer(User player) {
        this.player = player;
        this.hand = new Cards();
    }

    public String getUserEmail() {
        return player == null
                ? ""
                : player.getEmail();
    }

    public int cardsInHand() {
        return this.hand.size();
    }
}
