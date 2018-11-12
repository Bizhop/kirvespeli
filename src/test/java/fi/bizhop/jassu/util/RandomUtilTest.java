package fi.bizhop.jassu.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class RandomUtilTest {

    @Test
    public void win() {
        final int ROUNDS = 100000;
        final int PROBABILITY = 99;
        int wins = 0;

        for(int i=0; i<ROUNDS; i++) {
            if(RandomUtil.win(PROBABILITY)) {
                wins++;
            }
        }

        System.out.println(String.format("Probability: %d, wins: %d / %d", PROBABILITY, wins, ROUNDS));
    }
}