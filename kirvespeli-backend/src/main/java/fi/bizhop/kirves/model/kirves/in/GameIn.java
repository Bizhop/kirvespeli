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
    String dealer;

    @JsonCreator
    public static GameIn fromJson(
            @JsonProperty("action") Game.Action action,
            @JsonProperty("index") Integer index,
            @JsonProperty("keepExtraCard") Boolean keepExtraCard,
            @JsonProperty("suit") Card.Suit suit,
            @JsonProperty("declineCut") Boolean declineCut,
            @JsonProperty("speak") Game.Speak speak,
            @JsonProperty("dealer") String dealer) {
        return GameIn.builder()
                .action(action)
                .index(index == null ? 0 : index)
                .keepExtraCard(Boolean.TRUE.equals(keepExtraCard))
                .suit(suit)
                .declineCut(Boolean.TRUE.equals(declineCut))
                .speak(speak)
                .dealer(dealer)
                .build();
    }
}
