package fi.bizhop.jassu.model.kirves.out;

import java.util.List;
import java.util.Map;

public class GameOut {
    private List<PlayerOut> players;
    private List<String> messages;
    private int cardsInDeck;
    private String turn;
    private String dealer;
    private List<String> myCardsInHand;
    private String myExtraCard;
    private List<String> myAvailableActions;
    private boolean canJoin;
    private boolean canDeclineCut;
    private String trumpCard;
    private String trump;
    private String cutCard;
    private String secondCutCard;
    private Long id;
    private Integer playersTotal;
    private String firstCardSuit;
    private Map<String, Integer> scores;
    private List<Map<String, Integer>> scoresHistory;

    //TODO: remove when frontend is handling message as list
    @Deprecated
    private String message;

    public GameOut() {}

    public GameOut(List<PlayerOut> players,
                   int cardsInDeck,
                   String dealer,
                   String turn,
                   List<String> myCardsInHand,
                   String myExtraCard,
                   List<String> myAvailableActions,
                   String message,
                   List<String> messages,
                   boolean canJoin,
                   String trumpCard,
                   String trump,
                   boolean canDeclineCut,
                   String cutCard,
                   String secondCutCard,
                   Integer playersTotal,
                   String firstCardSuit,
                   Map<String, Integer> scores,
                   List<Map<String, Integer>> scoresHistory) {
        this.players = players;
        this.cardsInDeck = cardsInDeck;
        this.dealer = dealer;
        this.turn = turn;
        this.myCardsInHand = myCardsInHand;
        this.myExtraCard = myExtraCard;
        this.myAvailableActions = myAvailableActions;
        this.message = message;
        this.messages = messages;
        this.canJoin = canJoin;
        this.trumpCard = trumpCard;
        this.trump = trump;
        this.canDeclineCut = canDeclineCut;
        this.cutCard = cutCard;
        this.secondCutCard = secondCutCard;
        this.playersTotal = playersTotal;
        this.firstCardSuit = firstCardSuit;
        this.scores = scores;
        this.scoresHistory = scoresHistory;
    }

    public List<PlayerOut> getPlayers() {
        return this.players;
    }

    //TODO: remove when frontend is handling message as list
    @Deprecated
    public String getMessage() {
        return this.message;
    }

    public int getCardsInDeck() { return this.cardsInDeck; }

    public String getTurn() {
        return this.turn;
    }

    public String getDealer() {
        return this.dealer;
    }

    public List<String> getMyCardsInHand() {
        return this.myCardsInHand;
    }

    public boolean getCanJoin() {
        return this.canJoin;
    }

    public String getTrump() { return this.trump; }

    public String getTrumpCard() {
        return this.trumpCard;
    }

    public int getNumOfPlayedRounds() {
        if(this.players == null) {
            return 0;
        }

        return this.players.stream().mapToInt(playerOut -> playerOut.getRoundsWon().size()).sum();
    }

    public List<String> getMyAvailableActions() {
        return this.myAvailableActions;
    }

    public String getMyExtraCard() {
        return this.myExtraCard;
    }

    public boolean isCanDeclineCut() {
        return this.canDeclineCut;
    }

    public String getCutCard() {
        return this.cutCard;
    }

    public Long getId() {
        return this.id;
    }

    public GameOut setId(Long id) {
        this.id = id;
        return this;
    }

    public Integer getPlayersTotal() {
        return this.playersTotal;
    }

    public String getFirstCardSuit() {
        return this.firstCardSuit;
    }

    public Map<String, Integer> getScores() {
        return this.scores;
    }

    public List<Map<String, Integer>> getScoresHistory() {
        return this.scoresHistory;
    }

    public String getSecondCutCard() {
        return this.secondCutCard;
    }

    public List<String> getMessages() {
        return this.messages;
    }
}
