package fi.bizhop.jassu.model.kirves.out;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

@Data
@Builder
@Jacksonized
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
    Map<String, Integer> scores;
    List<Map<String, Integer>> scoresHistory;

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
