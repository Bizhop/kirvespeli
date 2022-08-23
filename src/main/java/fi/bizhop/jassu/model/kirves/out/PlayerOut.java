package fi.bizhop.jassu.model.kirves.out;

import fi.bizhop.jassu.model.kirves.Player;
import lombok.Builder;
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

    public String getEmail() {
        return this.email;
    }

    public int getCardsInHand() {
        return this.cardsInHand;
    }

    public List<String> getPlayedCards() {
        return this.playedCards;
    }

    public List<Integer> getRoundsWon() {
        return this.roundsWon;
    }

    public List<String> getAvailableActions() {
        return this.availableActions;
    }

    public String getExtraCard() {
        return this.extraCard;
    }

    @Override
    public String toString() {
        return this.email;
    }

    public String getNickname() {
        return this.nickname;
    }

    public boolean isDeclaredPlayer() {
        return this.declaredPlayer;
    }

    public boolean isFolded() {
        return this.folded;
    }

    public String getSpeak() {
        return this.speak;
    }
}
