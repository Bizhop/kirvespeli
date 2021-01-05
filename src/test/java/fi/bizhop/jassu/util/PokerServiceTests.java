package fi.bizhop.jassu.util;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.models.Cards;
import fi.bizhop.jassu.models.PokerGame;
import fi.bizhop.jassu.models.PokerHand;
import fi.bizhop.jassu.service.PokerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static fi.bizhop.jassu.models.PokerHand.Type.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PokerServiceTests {
    @Autowired
    PokerService service;

    @Test
    public void dummy() {
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
