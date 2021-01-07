package fi.bizhop.jassu.models;

import java.util.List;

public class KirvesGameOut {
    private String admin;
    private List<KirvesPlayerOut> players;
    private String message;
    private int cardsInDeck;

    public KirvesGameOut(String admin, List<KirvesPlayerOut> players, int cardsInDeck) {
        this.admin = admin;
        this.players = players;
        this.cardsInDeck = cardsInDeck;
    }

    public KirvesGameOut(String message) {
        this.message = message;
    }

    public String getAdmin() {
        return admin;
    }

    public List<KirvesPlayerOut> getPlayers() {
        return players;
    }

    public String getMessage() {
        return message;
    }

    public int getCardsInDeck() { return cardsInDeck; }
}
