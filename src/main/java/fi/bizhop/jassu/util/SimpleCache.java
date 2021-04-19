package fi.bizhop.jassu.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleCache<K, V> {
    private long ttl;
    private final Map<K, SimpleCacheItem> cacheItemMap = new ConcurrentHashMap<>();

    public SimpleCache(long ttl) {
        this.ttl = ttl;
    }

    public V get(K key) {
        SimpleCacheItem item = cacheItemMap.get(key);
        return item == null ? null : item.get();
    }

    public void put(K key, V value) {
        cacheItemMap.put(key, new SimpleCacheItem(now() + ttl, value));
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    private class SimpleCacheItem {
        private long expiresAt;
        private V item;

        private SimpleCacheItem(long expiresAt, V item) {
            this.expiresAt = expiresAt;
            this.item = item;
        }

        private V get() {
            return now() > this.expiresAt ? null : this.item;
        }
    }
}
