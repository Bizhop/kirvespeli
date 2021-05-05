package fi.bizhop.jassu.model.kirves.out;

import fi.bizhop.jassu.model.kirves.Player;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class PlayerOut {
    private String email;
    private String nickname;
    private int cardsInHand;
    private List<String> playedCards;
    private List<Integer> roundsWon;
    private List<String> availableActions;
    private String extraCard;
    private boolean declaredPlayer;
    private boolean folded;
    private String speak;

    public PlayerOut() {}

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
        return extraCard;
    }

    @Override
    public String toString() {
        return this.email;
    }

    public String getNickname() {
        return nickname;
    }

    public boolean isDeclaredPlayer() {
        return declaredPlayer;
    }

    public boolean isFolded() {
        return folded;
    }

    public String getSpeak() {
        return speak;
    }
}
