package fi.bizhop.jassu.util;


public class Lottery {
    private final int RETURN_PERCENTAGE;

    public Lottery(int returnPercentage) {
        this.RETURN_PERCENTAGE = returnPercentage;
    }

    public double doubleOrNothing(double wager) {
        if(RandomUtil.win(50)) {
            return wager * 2 * RETURN_PERCENTAGE / 100;
        }
        else {
            return 0;
        }
    }
}
