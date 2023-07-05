package liteweb.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionalLRUCache implements Cache {
    private final LinkedHashMap<String, byte[]> cache;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition hasWriter = lock.newCondition();
    private volatile boolean isWriting = false;

    public ConditionalLRUCache(int capacity) {
        this.cache = new LinkedHashMap<String, byte[]>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
                return size() > capacity;
            }
        };
    }

    @Override
    public Optional<byte[]> getByteCache(String key) throws InterruptedException {
        lock.lock();
        try {
            while (isWriting) {
                hasWriter.await();
            }
            return Optional.ofNullable(cache.get(key));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void putByteCache(String key, byte[] data) throws InterruptedException {
        lock.lock();
        try {
            while (isWriting) {
                hasWriter.await();
            }
            isWriting = true;
            cache.put(key, data);
            isWriting = false;
            hasWriter.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
