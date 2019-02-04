package fi.bizhop.jassu.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import static fi.bizhop.jassu.models.PokerGame.Action;

public class PokerGameOut {
    private List<String> hand;
    private String handValue;
    private Long gameId;
    private BigDecimal money;
    private String message;

    private List<Action> availableActions = new ArrayList<>();

    public PokerGameOut() {}

    public PokerGameOut(PokerGame game) {
        this.handValue = game.getHand().evaluate().toString();
        this.hand = game.getHand().getCardsOut();
        this.gameId = game.getGameId();
        this.availableActions = game.getAvailableActions();
        this.money = game.getMoney();
    }

    public PokerGameOut(String message) {
        this.message = message;
    }

    public List<String> getHand() {
        return hand;
    }

    public void setHand(List<String> hand) {
        this.hand = hand;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public List<Action> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List<Action> availableActions) {
        this.availableActions = availableActions;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public String getHandValue() {
        return handValue;
    }

    public void setHandValue(String handValue) {
        this.handValue = handValue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
