package fi.bizhop.jassu.models;

import fi.bizhop.jassu.service.UserService;

import java.math.BigDecimal;
import java.util.*;

public class PokerGame {
    private Cards deck = new StandardDeck().shuffle();
    private Cards hand;
    private Long gameId;
    private BigDecimal money;
    private List<Action> availableActions = Arrays.asList(Action.HOLD, Action.STAY);
    private String player;

    private static final Map<PokerHand.Type, BigDecimal> multipliers;

    static {
        multipliers = new HashMap<>();
        multipliers.put(PokerHand.Type.INVALID, new BigDecimal(0));
        multipliers.put(PokerHand.Type.HIGH, new BigDecimal(0));
        multipliers.put(PokerHand.Type.PAIR, new BigDecimal(0));
        multipliers.put(PokerHand.Type.TWO_PAIRS, new BigDecimal(2));
        multipliers.put(PokerHand.Type.THREE_OF_A_KIND, new BigDecimal(3));
        multipliers.put(PokerHand.Type.STRAIGHT, new BigDecimal(6));
        multipliers.put(PokerHand.Type.FLUSH, new BigDecimal(7));
        multipliers.put(PokerHand.Type.FULL_HOUSE, new BigDecimal(13));
        multipliers.put(PokerHand.Type.FOUR_OF_A_KIND, new BigDecimal(30));
        multipliers.put(PokerHand.Type.STRAIGHT_FLUSH, new BigDecimal(50));
    }

    public PokerGame(Long gameId, BigDecimal wager) {
        this.money = wager;
        this.gameId = gameId;
    }

    public Long getGameId() {
        return this.gameId;
    }

    public Cards getHand() {
        return this.hand;
    }

    public Cards getDeck() { return this.deck; }

    public BigDecimal getMoney() { return this.money; }

    public List<Action> getAvailableActions() { return this.availableActions; }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public PokerGame deal() {
        this.hand = this.deck.give(5);
        return this;
    }

    public void collect() {
        PokerHand ev = hand.evaluate();
        this.money = this.money.multiply(multipliers.get(ev.type));
        UserService.modifyMoney(this.money, this.player);
        this.availableActions = new ArrayList<>();
    }

    public boolean active() {
        return !this.availableActions.isEmpty();
    }

    public enum Action {
        HOLD, STAY
    }
}
