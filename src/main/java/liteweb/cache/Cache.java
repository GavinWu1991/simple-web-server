package liteweb.cache;

import java.util.Optional;

public interface Cache {
    Optional<byte[]> getByteCache(String key);

    void putByteCache(String key, byte[] data);
}
