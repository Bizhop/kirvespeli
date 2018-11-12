package fi.bizhop.jassu.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class LotteryTest {

    @Test
    public void doubleOrNothing() {
        double bank = 1000;
        double wager = 10;

        double bankMin = 1000;
        double bankMax = 1000;

        Lottery lot = new Lottery(98);

        for(int i=0; i<10000000; i++) {
            bank -= wager;
            bank += lot.doubleOrNothing(wager);
            if(bankMin > bank) {
                bankMin = bank;
            }
            if(bankMax < bank) {
                bankMax = bank;
            }
        }

        System.out.println(String.format("Bank: %f, (%f - %f)", bank, bankMin, bankMax));
    }
}