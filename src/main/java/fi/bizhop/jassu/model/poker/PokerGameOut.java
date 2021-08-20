package fi.bizhop.jassu.model.poker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static fi.bizhop.jassu.model.poker.PokerGame.Action;

public class PokerGameOut {
    private Long id;
    private String player;
    private List<String> hand;
    private String handValue;
    private BigDecimal money;
    private String message;
    private BigDecimal userMoney;
    private List<MultiplierOut> multipliersTable;

    private List<Action> availableActions = new ArrayList<>();

    public PokerGameOut() {}

    public PokerGameOut(PokerGame game, Long id) {
        this.setInitialValues(game, id);
        this.userMoney = BigDecimal.ZERO;
    }

    public PokerGameOut(PokerGame game, Long id, BigDecimal userMoney) {
        this.setInitialValues(game, id);
        this.userMoney = userMoney;
    }

    private void setInitialValues(PokerGame game, Long id) {
        this.id = id;
        this.player = game.getPlayer();
        this.handValue = game.getEvaluation().toString();
        this.hand = game.getHand().getCardsOut();
        this.availableActions = game.getAvailableActions();
        this.money = game.getMoney();
        this.multipliersTable = PokerGame.getMultiplierTable();
    }

    public PokerGameOut(String message) {
        this.message = message;
    }

    public List<String> getHand() {
        return this.hand;
    }

    public void setHand(List<String> hand) {
        this.hand = hand;
    }

    public List<Action> getAvailableActions() {
        return this.availableActions;
    }

    public void setAvailableActions(List<Action> availableActions) {
        this.availableActions = availableActions;
    }

    public BigDecimal getMoney() {
        return this.money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public String getHandValue() {
        return this.handValue;
    }

    public void setHandValue(String handValue) {
        this.handValue = handValue;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BigDecimal getUserMoney() {
        return this.userMoney;
    }

    public void setUserMoney(BigDecimal userMoney) {
        this.userMoney = userMoney;
    }

    public List<MultiplierOut> getMultipliersTable() {
        return this.multipliersTable;
    }

    public void setMultipliersTable(List<MultiplierOut> multipliersTable) {
        this.multipliersTable = multipliersTable;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }
}
