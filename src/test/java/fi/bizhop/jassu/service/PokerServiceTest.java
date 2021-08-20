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
    public void stayWithNoWinning() throws CardException {
//        var hand = Cards.fromAbbreviations(List.of(""));
        var game = this.pokerService.newGameForPlayer(getTestUser());

        game.stay(this.userService);

        var hand = game.getEvaluation();
        if(List.of(INVALID, HIGH, PAIR).contains(hand.type)) {
            assertEquals(BigDecimal.ZERO, game.getMoney());
        } else {
            assertTrue(game.getMoney().compareTo(BigDecimal.ZERO) > 0);
        }
    }
}
