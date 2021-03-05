package fi.bizhop.jassu.util;

import fi.bizhop.jassu.exception.ProbabilityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {
    private static final Logger LOG = LoggerFactory.getLogger(RandomUtil.class);

    public static boolean win(int probability) throws ProbabilityException {
        if(probability < 1 || probability > 99) {
            LOG.error(String.format("Not fair probability: %d%%", probability));
            throw new ProbabilityException("Probability should be between 1 and 99");
        }

        return ThreadLocalRandom.current().nextInt(100) < probability;
    }

    public static int getInt(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }
}
