package io.aeron.monitoring;

import io.aeron.CommonContext;
import io.aeron.monitoring.model.StreamKey;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Ivan Zemlyanskiy
 */
class CounterParserTest {

    public static Stream<Arguments> labels() {
        return Stream.of(
                arguments("sub-pos: 2 -1642393303 123 aeron:ipc @0", new StreamKey(CommonContext.IPC_MEDIA, 123)),
                arguments("sub-pos: 2 -1642393303 123 aeron:ipc", new StreamKey(CommonContext.IPC_MEDIA, 123)),
                arguments(
                        "snd-pos: 2 1707220681 10 aeron:udp?endpoint=localhost:40123",
                        new StreamKey("localhost:40123", 10)),
                arguments(
                        "snd-pos: 2 1707220681 10 aeron:udp?endpoint=localhost:40123 @128",
                        new StreamKey("localhost:40123", 10)),
                arguments("pub-lmt: 1 -140882376 123 aeron:ipc", new StreamKey(CommonContext.IPC_MEDIA, 123)),
                arguments("pub-pos (sampled): 1 -140882376 123 aeron:ipc", new StreamKey(CommonContext.IPC_MEDIA, 123)),
                arguments("sub-pos: 2 -1642393303 123 aeron:ipc @0", new StreamKey(CommonContext.IPC_MEDIA, 123))
        );
    }

    @ParameterizedTest
    @MethodSource("labels")
    void shouldExtractEndpointFromLabel(String label, StreamKey result) {
        StreamKey key = CounterParser.extractStreamKeyFromLabel(label);
        assertEquals(key, result);
    }
}