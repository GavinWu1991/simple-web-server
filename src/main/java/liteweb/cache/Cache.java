package liteweb.cache;

import java.util.Optional;

public interface Cache {
    Optional<byte[]> getByteCache(String key) throws InterruptedException;

    void putByteCache(String key, byte[] data) throws InterruptedException;
}
