package fi.bizhop.jassu.util;

import fi.bizhop.jassu.exception.ProbabilityException;
import org.junit.Test;

import static org.junit.Assert.*;

public class RandomUtilTest {

    @Test
    public void win() {
        final int ROUNDS = 100000;
        final int PROBABILITY = 99;
        int wins = 0;

        for(int i=0; i<ROUNDS; i++) {
            try {
                if(RandomUtil.win(PROBABILITY)) {
                    wins++;
                }
            } catch (ProbabilityException e) {}
        }

        System.out.println(String.format("Probability: %d, wins: %d / %d", PROBABILITY, wins, ROUNDS));
    }

    @Test
    public void reasonableProbabilities() {
        try {
            RandomUtil.win(0);
            fail("0 should not be fair probability");
        } catch (ProbabilityException e) {}
        try {
            RandomUtil.win(100);
            fail("100 should not be fair probability");
        } catch (ProbabilityException e) {}
    }

    @Test
    public void massTestGetInt() {
        final int REPETITIONS = 1000000;
        final int MAX = 10;

        long begin = System.currentTimeMillis();
        for(int i = 0; i < REPETITIONS; i++) {
            int number = RandomUtil.getInt(MAX);
            assertTrue(number >= 0 && number < MAX);
        }
        System.out.printf("Testing time: %d ms", System.currentTimeMillis() - begin);
    }
}