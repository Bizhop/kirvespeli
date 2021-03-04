package fi.bizhop.jassu.model;

import java.util.List;

public class KirvesGameOut {
    private Long id;
    private String admin;
    private List<KirvesPlayerOut> players;
    private String message;
    private int cardsInDeck;
    private String turn;
    private String dealer;
    private List<String> myCardsInHand;
    private String myExtraCard;
    private List<String> myAvailableActions;
    private boolean canJoin;
    private String valttiKortti;
    private String valtti;

    public KirvesGameOut() {}

    public KirvesGameOut(Long id, String admin, List<KirvesPlayerOut> players, int cardsInDeck, String dealer, String turn, List<String> myCardsInHand, String myExtraCard, List<String> myAvailableActions, String message, boolean canJoin, String valttiKortti, String valtti) {
        this.id = id;
        this.admin = admin;
        this.players = players;
        this.cardsInDeck = cardsInDeck;
        this.dealer = dealer;
        this.turn = turn;
        this.myCardsInHand = myCardsInHand;
        this.myExtraCard = myExtraCard;
        this.myAvailableActions = myAvailableActions;
        this.message = message;
        this.canJoin = canJoin;
        this.valttiKortti = valttiKortti;
        this.valtti = valtti;
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

    public String getTurn() {
        return turn;
    }

    public String getDealer() {
        return dealer;
    }

    public List<String> getMyCardsInHand() {
        return myCardsInHand;
    }

    public Long getId() {
        return id;
    }

    public boolean getCanJoin() {
        return canJoin;
    }

    public String getValtti() { return valtti; }

    public String getValttiKortti() {
        return valttiKortti;
    }

    public int getNumOfPlayedRounds() {
        if(players == null) {
            return 0;
        }

        return players.stream().map(player -> player.getPlayedCards().size()).mapToInt(value -> value).min().orElse(0);
    }

    public List<String> getMyAvailableActions() {
        return myAvailableActions;
    }
}
