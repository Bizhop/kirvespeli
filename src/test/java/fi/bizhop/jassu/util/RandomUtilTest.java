package fi.bizhop.jassu.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RandomUtilTest {
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