package fi.bizhop.jassu.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SimpleCacheTest {
    private final long WAIT = 100;
    private final long TTL = 1000;

    @Test
    public void testSimpleCache() throws InterruptedException {
        SimpleCache<String, String> cache = new SimpleCache<>(TTL);
        cache.put("key", "data");
        cache.put("key2", "data2");

        assertEquals("data", cache.get("key"));
        assertEquals("data2", cache.get("key2"));

        Thread.sleep(WAIT);
        assertEquals("data", cache.get("key"));
        assertEquals("data2", cache.get("key2"));

        Thread.sleep(TTL);
        assertNull(cache.get("key"));
        assertNull(cache.get("key2"));
    }
}
