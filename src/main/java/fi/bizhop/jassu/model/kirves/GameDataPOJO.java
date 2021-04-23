package fi.bizhop.jassu.model.kirves;

import java.util.List;

public class GameDataPOJO {
    public List<PlayerPOJO> players;
    public List<String> deck;
    public boolean canJoin;
    public String turn;
    public String dealer;
    public boolean canDeal;
    public String message;
    public String firstPlayerOfRound;
    public String valttiCard;
    public String valtti;
    public String cutCard;
    public boolean canSetValtti;
    public boolean forcedGame;
    public boolean canDeclineCut;

    public GameDataPOJO() {}

    public GameDataPOJO(List<PlayerPOJO> players, List<String> deck, boolean canJoin, String turn, String dealer, boolean canDeal, String message, String firstPlayerOfRound, String valttiCard, String valtti, String cutCard, boolean canSetValtti, boolean forcedGame, boolean canDeclineCut) {
        this.players = players;
        this.deck = deck;
        this.canJoin = canJoin;
        this.turn = turn;
        this.dealer = dealer;
        this.canDeal = canDeal;
        this.message = message;
        this.firstPlayerOfRound = firstPlayerOfRound;
        this.valttiCard = valttiCard;
        this.valtti = valtti;
        this.cutCard = cutCard;
        this.canSetValtti = canSetValtti;
        this.forcedGame = forcedGame;
        this.canDeclineCut = canDeclineCut;
    }
}
