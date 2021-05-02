package fi.bizhop.jassu.model.kirves;

import java.util.List;

public class GameOut {
    private List<PlayerOut> players;
    private String message;
    private int cardsInDeck;
    private String turn;
    private String dealer;
    private List<String> myCardsInHand;
    private String myExtraCard;
    private List<String> myAvailableActions;
    private boolean canJoin;
    private boolean canDeclineCut;
    private String valttiKortti;
    private String valtti;
    private String cutCard;
    private Long id;
    private Integer playersTotal;
    private String firstCardSuit;

    public GameOut() {}

    public GameOut(List<PlayerOut> players,
                   int cardsInDeck,
                   String dealer,
                   String turn,
                   List<String> myCardsInHand,
                   String myExtraCard,
                   List<String> myAvailableActions,
                   String message,
                   boolean canJoin,
                   String valttiKortti,
                   String valtti,
                   boolean canDeclineCut,
                   String cutCard,
                   Integer playersTotal,
                   String firstCardSuit) {
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
        this.canDeclineCut = canDeclineCut;
        this.cutCard = cutCard;
        this.playersTotal = playersTotal;
        this.firstCardSuit = firstCardSuit;
    }

    public List<PlayerOut> getPlayers() {
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

        return players.stream().mapToInt(playerOut -> playerOut.getRoundsWon().size()).sum();
    }

    public List<String> getMyAvailableActions() {
        return myAvailableActions;
    }

    public String getMyExtraCard() {
        return myExtraCard;
    }

    public boolean isCanDeclineCut() {
        return canDeclineCut;
    }

    public String getCutCard() {
        return cutCard;
    }

    public Long getId() {
        return id;
    }

    public GameOut setId(Long id) {
        this.id = id;
        return this;
    }

    public Integer getPlayersTotal() {
        return playersTotal;
    }

    public void setPlayersTotal(Integer playersTotal) {
        this.playersTotal = playersTotal;
    }

    public String getFirstCardSuit() {
        return firstCardSuit;
    }
}
