package fi.bizhop.kirves.model.kirves.in;

import fi.bizhop.kirves.model.Card;
import fi.bizhop.kirves.model.kirves.Game;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class GameIn {
    Game.Action action;
    int index;
    boolean keepExtraCard;
    Card.Suit suit;
    boolean declineCut;
    Game.Speak speak;
}
