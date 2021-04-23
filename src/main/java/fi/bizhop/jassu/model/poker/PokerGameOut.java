package fi.bizhop.jassu.model.poker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static fi.bizhop.jassu.model.poker.PokerGame.Action;

public class PokerGameOut {
    private List<String> hand;
    private String handValue;
    private BigDecimal money;
    private String message;
    private BigDecimal userMoney;
    private List<MultiplierOut> multipliersTable;

    private List<Action> availableActions = new ArrayList<>();

    public PokerGameOut() {}

    public PokerGameOut(PokerGame game) {
        this.setInitialValues(game, BigDecimal.valueOf(0));
    }

    public PokerGameOut(PokerGame game, BigDecimal userMoney) {
        this.setInitialValues(game, userMoney);
    }

    private void setInitialValues(PokerGame game, BigDecimal userMoney) {
        this.handValue = game.getEvaluation().toString();
        this.hand = game.getHand().getCardsOut();
        this.availableActions = game.getAvailableActions();
        this.money = game.getMoney();
        this.userMoney = userMoney;
        this.multipliersTable = PokerGame.getMultiplierTable();
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

    public BigDecimal getUserMoney() {
        return userMoney;
    }

    public void setUserMoney(BigDecimal userMoney) {
        this.userMoney = userMoney;
    }

    public List<MultiplierOut> getMultipliersTable() {
        return multipliersTable;
    }

    public void setMultipliersTable(List<MultiplierOut> multipliersTable) {
        this.multipliersTable = multipliersTable;
    }
}
