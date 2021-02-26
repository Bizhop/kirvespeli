package fi.bizhop.jassu.model;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class KirvesPlayerOut {
    private String email;
    private int cardsInHand;
    private List<String> playedCards;
    private List<Integer> roundsWon;
    private List<String> availableActions;

    public KirvesPlayerOut() {}

    public KirvesPlayerOut(KirvesPlayer player) {
        this.email = player.getUserEmail();
        this.cardsInHand = player.cardsInHand();
        this.playedCards = player.getPlayedCards().getCardsOut();
        this.roundsWon = player.getRoundsWon();
        this.availableActions = player.getAvailableActions().stream()
                .map(Enum::name)
                .collect(toList());
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
}
