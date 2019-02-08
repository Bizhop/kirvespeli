package fi.bizhop.jassu.models;

import fi.bizhop.jassu.service.UserService;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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
        multipliers.put(PokerHand.Type.INVALID, BigDecimal.valueOf(0L));
        multipliers.put(PokerHand.Type.HIGH, BigDecimal.valueOf(0L));
        multipliers.put(PokerHand.Type.PAIR, BigDecimal.valueOf(0L));
        multipliers.put(PokerHand.Type.TWO_PAIRS, BigDecimal.valueOf(2L));
        multipliers.put(PokerHand.Type.THREE_OF_A_KIND, BigDecimal.valueOf(3L));
        multipliers.put(PokerHand.Type.STRAIGHT, BigDecimal.valueOf(6L));
        multipliers.put(PokerHand.Type.FLUSH, BigDecimal.valueOf(7L));
        multipliers.put(PokerHand.Type.FULL_HOUSE, BigDecimal.valueOf(13L));
        multipliers.put(PokerHand.Type.FOUR_OF_A_KIND, BigDecimal.valueOf(30L));
        multipliers.put(PokerHand.Type.STRAIGHT_FLUSH, BigDecimal.valueOf(50L));
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

    public static List<MultiplierOut> getMultiplierTable() {
        return multipliers.entrySet().stream()
                .filter(e -> !e.getValue().equals(BigDecimal.valueOf(0L)))
                .map(e -> new MultiplierOut(e.getKey().getText(), e.getValue()))
                .sorted()
                .collect(Collectors.toList());
    }

    public enum Action {
        HOLD, STAY
    }
}
