package fi.bizhop.kirves.model.kirves.out;

import fi.bizhop.kirves.model.kirves.Player;
import lombok.Value;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Value
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
    boolean inGame;
    boolean departurePending;
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
        this.inGame = player.isInGame();
        this.departurePending = player.isDeparturePending();
        this.speak = player.getSpeak() == null ? null : player.getSpeak().name();
    }
}
