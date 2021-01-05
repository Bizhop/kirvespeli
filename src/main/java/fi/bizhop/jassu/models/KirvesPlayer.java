package fi.bizhop.jassu.models;

public class KirvesPlayer {
    private User player;
    private Cards hand;

    public KirvesPlayer(User player) {
        this.player = player;
    }

    public String getUserEmail() {
        return player == null
                ? ""
                : player.getEmail();
    }
}
