package fi.bizhop.jassu.model.poker;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.model.Card;
import fi.bizhop.jassu.model.Cards;
import fi.bizhop.jassu.model.StandardDeck;
import fi.bizhop.jassu.service.UserService;
import fi.bizhop.jassu.util.PokerHandEvaluator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fi.bizhop.jassu.model.poker.PokerHand.Type;

public class PokerGame {

    private final Cards deck;
    private Cards hand;
    private BigDecimal money;
    private List<Action> availableActions = List.of(Action.HOLD);
    private String player;
    private int doubles = 0;
    private PokerHand evaluation;

    private static final Map<Type, BigDecimal> multipliers;

    static {
        multipliers = new HashMap<>();
        multipliers.put(Type.INVALID, BigDecimal.valueOf(0L));
        multipliers.put(Type.HIGH, BigDecimal.valueOf(0L));
        multipliers.put(Type.PAIR, BigDecimal.valueOf(0L));
        multipliers.put(Type.TWO_PAIRS, BigDecimal.valueOf(2L));
        multipliers.put(Type.THREE_OF_A_KIND, BigDecimal.valueOf(3L));
        multipliers.put(Type.STRAIGHT, BigDecimal.valueOf(6L));
        multipliers.put(Type.FLUSH, BigDecimal.valueOf(7L));
        multipliers.put(Type.FULL_HOUSE, BigDecimal.valueOf(13L));
        multipliers.put(Type.FOUR_OF_A_KIND, BigDecimal.valueOf(30L));
        multipliers.put(Type.STRAIGHT_FLUSH, BigDecimal.valueOf(50L));
    }

    public PokerGame(BigDecimal wager) throws CardException {
        this.deck = new StandardDeck().shuffle();
        this.money = wager;
    }

    public Cards getHand() {
        return this.hand;
    }

    public Cards getDeck() { return this.deck; }

    public BigDecimal getMoney() { return this.money; }

    public List<Action> getAvailableActions() { return this.availableActions; }

    public String getPlayer() {
        return this.player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public void deal(Cards hand) throws CardException {
        this.hand = hand == null ?
                this.deck.deal(5) :
                this.deck.giveCards(hand);
        this.evaluation = PokerHandEvaluator.evaluate(this.hand);
    }

    public void stay(UserService userService) {
        if(this.doubles == 0) {
            this.money = this.money.multiply(multipliers.get(this.evaluation.type));
        }
        userService.modifyMoney(this.money, this.player);
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

    public void tryDouble(Action action, UserService userService) throws CardException {
        if(this.doubles < 5) {
            if(this.doubles == 0) {
                this.hand.clear();
            }
            Card doubleCard = this.deck.deal(1).first();
            this.hand.add(doubleCard);
            int doubleValue = doubleCard.getRank().getValue();
            if(doubleValue == 14) {
                doubleValue = 1;
            }
            if((action == Action.DOUBLE_HIGH && doubleValue > 7) || action == Action.DOUBLE_LOW && doubleValue < 7 ) {
                this.money = this.money.multiply(BigDecimal.valueOf(2));
                this.doubles++;
                if(this.doubles > 4) {
                    this.stay(userService);
                }
            }
            else {
                this.money = BigDecimal.valueOf(0);
                this.availableActions = new ArrayList<>();
            }
        }
        else {
            this.stay(userService);
        }
    }

    public void hold(List<Integer> parameters) throws CardException {
        this.hand.hold(parameters, this.getDeck());
        this.evaluation = PokerHandEvaluator.evaluate(this.hand);
        this.money = this.money.multiply(multipliers.get(this.evaluation.type));
        this.availableActions = this.money.equals(BigDecimal.valueOf(0)) ? new ArrayList<>() : List.of(Action.STAY, Action.DOUBLE_HIGH, Action.DOUBLE_LOW);
    }

    public PokerHand getEvaluation() {
        return this.evaluation;
    }

    public enum Action {
        HOLD, STAY, DOUBLE_HIGH, DOUBLE_LOW
    }
}
