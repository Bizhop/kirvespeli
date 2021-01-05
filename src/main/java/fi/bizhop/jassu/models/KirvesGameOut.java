package fi.bizhop.jassu.models;

import java.util.List;

public class KirvesGameOut {
    private String admin;
    private List<String> players;
    private String message;

    public KirvesGameOut(KirvesGame game) {
        if(game != null) {
            this.admin = game.getAdmin();
            this.players = game.getPlayers();
        }
        else {
            this.message = "Game is null";
        }
    }

    public KirvesGameOut(String message) {
        this.message = message;
    }

    public String getAdmin() {
        return admin;
    }

    public List<String> getPlayers() {
        return players;
    }

    public String getMessage() {
        return message;
    }
}
