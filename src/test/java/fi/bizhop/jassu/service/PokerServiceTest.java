package fi.bizhop.jassu.service;


import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.PokerGameException;
import fi.bizhop.jassu.model.Cards;
import fi.bizhop.jassu.model.poker.PokerGameIn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;

import static fi.bizhop.jassu.model.poker.PokerGame.Action.HOLD;
import static fi.bizhop.jassu.util.TestUserUtil.getTestUser;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class PokerServiceTest {
    @MockBean
    UserService userService;

    PokerService pokerService;

    @BeforeEach
    public void setup() {
        this.pokerService = new PokerService(this.userService);
    }

    @Test
    public void newGame() throws CardException {
        var game = this.pokerService.newGame();

        System.out.printf("initialized game for user %s%n", game.getPlayer());
        System.out.println(game.getHand());
    }

    @Test
    public void holdAllWithHighCardZeroMoney() throws CardException, PokerGameException {
        var user = getTestUser();

        var hand = Cards.fromAbbreviations(List.of("KH", "QS", "JH", "TC", "6S"));
        var id = this.pokerService.newGameForPlayer(user, hand).getId();

        var input = new PokerGameIn();
        input.action = HOLD;
        input.parameters = List.of(0,1,2,3,4);

        var game = this.pokerService.action(id, input, user);

        assertEquals(BigDecimal.ZERO, game.getMoney());
    }

    @Test
    public void holdAllWithTwoPairDoubleMoney() throws CardException, PokerGameException {
        var user = getTestUser();

        var hand = Cards.fromAbbreviations(List.of("KH", "KS", "JH", "JC", "6S"));
        var id = this.pokerService.newGameForPlayer(user, hand).getId();

        var input = new PokerGameIn();
        input.action = HOLD;
        input.parameters = List.of(0,1,2,3,4);

        var game = this.pokerService.action(id, input, user);

        assertEquals(BigDecimal.valueOf(2), game.getMoney());
    }
}
