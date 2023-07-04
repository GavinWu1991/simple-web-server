package liteweb.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class LRUCache implements Cache {
    private final LinkedHashMap<String, byte[]> cache;
    private final SimpleWriteFirstLock lock = new SimpleWriteFirstLock();

    public LRUCache(int capacity) {
        this.cache = new LinkedHashMap<String, byte[]>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
                return size() > capacity;
            }
        };
    }

    @Override
    public Optional<byte[]> getByteCache(String key) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(cache.get(key));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void putByteCache(String key, byte[] data) {
        lock.writeLock().lock();
        try {
            cache.put(key, data);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
