package fi.bizhop.jassu.model.kirves;

import java.util.List;

public class GameDataPOJO {
    //these values are only updated when generating save data
    public List<PlayerPOJO> players;
    public List<String> deck;
    public String turn;
    public String dealer;
    public String firstPlayerOfRound;
    public String valttiCard;
    public String valtti;
    public String cutCard;

    //these values are updated running the game and evaluated for Game equality
    public boolean canJoin;
    public boolean canDeal;
    public String message;
    public boolean canSetValtti;
    public boolean forcedGame;
    public boolean canDeclineCut;

    public GameDataPOJO() {}

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof GameDataPOJO)) return false;
        GameDataPOJO other = (GameDataPOJO) o;

        if(this.message == null && other.message != null) return false;

        return this.canJoin == other.canJoin
                && this.canDeal == other.canDeal
                && (this.message == null || this.message.equals(other.message))
                && this.canSetValtti == other.canSetValtti
                && this.forcedGame == other.forcedGame
                && this.canDeclineCut == other.canDeclineCut;
    }
}
