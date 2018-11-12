package fi.bizhop.jassu.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {
    private static final Logger LOG = LoggerFactory.getLogger(RandomUtil.class);

    public static boolean win(int probability) {
        if(probability < 1 || probability > 99) {
            LOG.error(String.format("Not fair probability: %d%%", probability));
        }

        return ThreadLocalRandom.current().nextInt(100) < probability;
    }
}
