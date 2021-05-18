package fi.bizhop.jassu.model.kirves.pojo;

import java.util.*;

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
    public String secondCutCard;

    //these values are updated running the game and evaluated for GameDataPOJO equality
    public Map<String, ScorePOJO> scores = new LinkedHashMap<>();
    public List<Map<String, ScorePOJO>> scoresHistory = new ArrayList<>();
    public boolean canJoin = false;
    public boolean canDeal = false;
    public String message;
    public boolean forcedGame = false;
    public boolean canDeclineCut = false;
    public boolean speaking = false;

    public GameDataPOJO() {}

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof GameDataPOJO)) return false;
        GameDataPOJO other = (GameDataPOJO) o;

        if(this.message == null && other.message != null) return false;

        return this.scores.equals(other.scores)
                && this.scoresHistory.equals(other.scoresHistory)
                && this.canJoin == other.canJoin
                && this.canDeal == other.canDeal
                && (this.message == null || this.message.equals(other.message))
                && this.forcedGame == other.forcedGame
                && this.canDeclineCut == other.canDeclineCut
                && this.speaking == other.speaking;
    }
}
