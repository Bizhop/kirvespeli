package fi.bizhop.jassu.util;


import fi.bizhop.jassu.exception.ProbabilityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lottery {
    private final static Logger LOG = LoggerFactory.getLogger(Lottery.class);

    private final int RETURN_PERCENTAGE;

    public Lottery(int returnPercentage) {
        this.RETURN_PERCENTAGE = returnPercentage;
    }

    public double doubleOrNothing(double wager) {
        return this.anythingGoes(wager, 50);
    }

    public double anythingGoes(double wager, int probability) {
        try {
            if(RandomUtil.win(probability)) {
                return wager / probability * this.RETURN_PERCENTAGE;
            }
            else {
                return 0.0d;
            }
        } catch (ProbabilityException e) {
            LOG.error("Unfair probability", e);
            return wager;
        }
    }
}
