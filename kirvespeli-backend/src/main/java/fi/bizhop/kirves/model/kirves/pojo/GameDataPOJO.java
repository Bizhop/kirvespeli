package fi.bizhop.kirves.model.kirves.pojo;

import java.util.*;

public class GameDataPOJO {
    //these values are only updated when generating save data
    public List<PlayerPOJO> players;
    public List<String> deck;
    public String admin;
    public String turn;
    public String dealer;
    public String firstPlayerOfRound;
    public String trumpCard;
    public String trump;
    public String cutCard;
    public String secondCutCard;
    //epoch millis when pending departures are acknowledged automatically; wall-clock, not compared
    public Long departureDeadline;

    //these values are updated running the game and evaluated for GameDataPOJO equality
    //scores in seating order
    public List<ScorePOJO> scores = new ArrayList<>();
    public List<ScoreHistoryPOJO> scoresHistory = new ArrayList<>();
    public boolean canJoin = false;
    public boolean canDeal = false;
    public List<String> messages = new ArrayList<>();
    public boolean forcedGame = false;
    public boolean canDeclineCut = false;
    public boolean speaking = false;
    public Long currentHandId = null;

    public GameDataPOJO() {}

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof GameDataPOJO)) return false;
        var other = (GameDataPOJO) o;
        if(this.currentHandId != null && other.currentHandId == null) return false;

        return this.scores.equals(other.scores)
                && this.scoresHistory.equals(other.scoresHistory)
                && this.messages.equals(other.messages)
                && this.canJoin == other.canJoin
                && this.canDeal == other.canDeal
                && this.forcedGame == other.forcedGame
                && this.canDeclineCut == other.canDeclineCut
                && this.speaking == other.speaking
                && (this.currentHandId == null || this.currentHandId.equals(other.currentHandId));
    }
}
