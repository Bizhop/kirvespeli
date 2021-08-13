package fi.bizhop.jassu.util;

import org.junit.jupiter.api.Test;

public class LotteryTest {

    @Test
    public void doubleOrNothing() {
        final int ROUNDS = 10000;
        double bank = 1000;
        double wager = 10;
        double winnings = 0;

        double bankMin = 1000;
        double bankMax = 1000;

        Lottery lot = new Lottery(98);

        for(int i=0; i<ROUNDS; i++) {
            double winning = lot.doubleOrNothing(wager);
            winnings += winning;
            bank += winning - wager;
            if(bankMin > bank) {
                bankMin = bank;
            }
            if(bankMax < bank) {
                bankMax = bank;
            }
        }

        System.out.printf("Bank: %f, (%f - %f)%n", bank, bankMin, bankMax);
        System.out.printf("Wagered: %f, winnings: %f, percentage: %f%n", ROUNDS * wager, winnings, winnings/(ROUNDS * wager));
    }

    @Test
    public void anythingGoes() {
        final int ROUNDS = 10000;
        double bank = 1000;
        double wager = 10;
        double winnings = 0;

        double bankMin = 1000;
        double bankMax = 1000;

        Lottery lot = new Lottery(98);

        for(int i=0; i<ROUNDS; i++) {
            double winning = lot.anythingGoes(wager, 90);
            winnings += winning;
            bank += winning - wager;
            if(bankMin > bank) {
                bankMin = bank;
            }
            if(bankMax < bank) {
                bankMax = bank;
            }
        }

        System.out.printf("Bank: %f, (%f - %f)%n", bank, bankMin, bankMax);
        System.out.printf("Wagered: %f, winnings: %f, percentage: %f%n", ROUNDS * wager, winnings, winnings/(ROUNDS * wager));
    }
}