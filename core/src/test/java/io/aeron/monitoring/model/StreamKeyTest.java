package io.aeron.monitoring.model;

import io.aeron.CommonContext;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ivan Zemlyanskiy
 */
class StreamKeyTest {


    private final Set<StreamKey> set = new HashSet<>();

    @Test
    void verifyEqualsAndHashcode() {
        int count = 10;
        for (int i = 0; i < count; i++) {
            set.add(new StreamKey(CommonContext.IPC_MEDIA, i));
        }
        assertEquals(count, set.size());

        for (int i = 0; i < count; i++) {
            set.add(new StreamKey(CommonContext.IPC_MEDIA, i));
        }
        assertEquals(count, set.size());

        set.add(new StreamKey("localhost:1234", 1));
        assertEquals(count + 1, set.size());
    }
}