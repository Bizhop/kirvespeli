package fi.bizhop.kirves.model.kirves.out;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GameOut {
    List<PlayerOut> players;
    List<String> messages;
    int cardsInDeck;
    String turn;
    String dealer;
    List<String> myCardsInHand;
    String myExtraCard;
    List<String> myAvailableActions;
    boolean canJoin;
    boolean canDeclineCut;
    String trumpCard;
    String trump;
    String cutCard;
    String secondCutCard;
    Long id;
    Integer playersTotal;
    String firstCardSuit;
    List<ScoreOut> scores;
    List<ScoreHistoryOut> scoresHistory;

    public int getNumOfPlayedRounds() {
        if(this.players == null) {
            return 0;
        }

        return this.players.stream().mapToInt(playerOut -> playerOut.getRoundsWon().size()).sum();
    }

    public GameOut setId(Long id) {
        this.id = id;
        return this;
    }
}
