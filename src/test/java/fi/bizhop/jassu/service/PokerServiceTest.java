package fi.bizhop.jassu.service;


import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.model.Cards;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;

import static fi.bizhop.jassu.model.poker.PokerHand.Type.*;
import static fi.bizhop.jassu.util.TestUserUtil.getTestUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void stayWithHighCardZeroMoney() throws CardException {
        var hand = Cards.fromAbbreviations(List.of("KH", "QS", "JH", "TC", "6S"));
        var game = this.pokerService.newGameForPlayer(getTestUser(), hand);

        game.stay(this.userService);
        assertEquals(BigDecimal.ZERO, game.getMoney());
    }

    @Test
    public void stayWithTwoPairDoubleMoney() throws CardException {
        var hand = Cards.fromAbbreviations(List.of("KH", "KS", "JH", "JC", "6S"));
        var game = this.pokerService.newGameForPlayer(getTestUser(), hand);

        game.stay(this.userService);
        assertEquals(BigDecimal.valueOf(2), game.getMoney());
    }
}
