package fi.bizhop.kirves.util;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {
    public static int getInt(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }
}
