package fi.bizhop.jassu.model.kirves.out;

import fi.bizhop.jassu.model.kirves.Player;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Value
@Jacksonized
public class PlayerOut {
    String email;
    String nickname;
    int cardsInHand;
    List<String> playedCards;
    List<Integer> roundsWon;
    List<String> availableActions;
    String extraCard;
    boolean declaredPlayer;
    boolean folded;
    String speak;

    public PlayerOut(Player player) {
        this.email = player.getUserEmail();
        this.nickname = player.getUserNickname();
        this.cardsInHand = player.cardsInHand();
        this.playedCards = player.getPlayedCards().getCardsOut();
        this.roundsWon = player.getRoundsWon();
        this.availableActions = player.getAvailableActions().stream()
                .map(Enum::name)
                .collect(toList());
        this.extraCard = player.getExtraCard() != null ? player.getExtraCard().toString() : null;
        this.declaredPlayer = player.isDeclaredPlayer();
        this.folded = player.isFolded();
        this.speak = player.getSpeak() == null ? null : player.getSpeak().name();
    }
}
