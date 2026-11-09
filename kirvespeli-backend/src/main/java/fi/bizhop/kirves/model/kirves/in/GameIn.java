package fi.bizhop.kirves.model.kirves.in;

import fi.bizhop.kirves.model.Card;
import fi.bizhop.kirves.model.kirves.Game;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GameIn {
    Game.Action action;
    int index;
    boolean keepExtraCard;
    Card.Suit suit;
    boolean declineCut;
    Game.Speak speak;

    @JsonCreator
    public static GameIn fromJson(
            @JsonProperty("action") Game.Action action,
            @JsonProperty("index") int index,
            @JsonProperty("keepExtraCard") boolean keepExtraCard,
            @JsonProperty("suit") Card.Suit suit,
            @JsonProperty("declineCut") boolean declineCut,
            @JsonProperty("speak") Game.Speak speak) {
        return GameIn.builder()
                .action(action)
                .index(index)
                .keepExtraCard(keepExtraCard)
                .suit(suit)
                .declineCut(declineCut)
                .speak(speak)
                .build();
    }
}
