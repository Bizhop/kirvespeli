package fi.bizhop.jassu;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.models.Cards;
import fi.bizhop.jassu.models.PokerGame;
import fi.bizhop.jassu.models.PokerHand;
import fi.bizhop.jassu.service.PokerService;
import fi.bizhop.jassu.service.UserService;
import fi.bizhop.jassu.util.PokerHandEvaluator;
import org.junit.BeforeClass;
import org.junit.Test;

import static fi.bizhop.jassu.models.PokerHand.Type.*;
import static org.mockito.Mockito.mock;

public class PokerServiceTest {
    static UserService userService;

    @BeforeClass
    public static void setup() {
        userService = mock(UserService.class);
    }

    @Test
    public void dummy() {
        PokerService service = new PokerService(userService);
        service.dummy();
    }

    //don't run this with builds (comment out next line always)
    //@Test
    public void testHand() throws CardException {
        getHand(HIGH);
        getHand(PAIR);
        getHand(TWO_PAIRS);
        getHand(THREE_OF_A_KIND);
        getHand(STRAIGHT);
        getHand(FLUSH);
        getHand(FULL_HOUSE);
        getHand(FOUR_OF_A_KIND);
        getHand(STRAIGHT_FLUSH);
    }

    private void getHand(PokerHand.Type type) throws CardException {
        PokerService service = new PokerService(userService);
        long time = System.currentTimeMillis();
        int reps = 0;
        boolean run = true;
        while(run) {
            reps++;
            PokerGame game = service.newGame().deal();
            Cards hand = game.getHand();
            PokerHand ev = PokerHandEvaluator.evaluate(hand);
            if(ev.type == type) {
                System.out.println(ev);
                System.out.println(hand);
                System.out.println(String.format("Reps: %d, time: %dms", reps, System.currentTimeMillis() - time));
                run = false;
            }
        }
    }
}
