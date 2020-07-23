package io.aeron.monitoring;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.aeron.*;
import io.aeron.driver.MediaDriver;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.monitoring.model.*;
import org.agrona.collections.MutableBoolean;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingMillisIdleStrategy;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.aeron.driver.status.SystemCounterDescriptor.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CncReaderManyPubManySubTest {
    public static final String CHANNEL = "aeron:udp?endpoint=localhost:1234";
    public static final int STREAM_ID_ONE = 123;
    public static final int STREAM_ID_TWO = 321;
    public static final int MESSAGE_SIZE = 100;

    private final IdleStrategy idleStrategy = new SleepingMillisIdleStrategy(1);

    @ParameterizedTest
    @ValueSource(strings = {"aeron:udp?endpoint=localhost:1234", "aeron:ipc"})
    void shouldReadManyPublishersManySubscribers() {
        try (MediaDriver mediaDriver = MediaDriver.launchEmbedded(new MediaDriver.Context().dirDeleteOnShutdown(false));
             Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(mediaDriver.aeronDirectoryName()));
             Publication publicationOne = aeron.addPublication(CHANNEL + "|alias=pub-one", STREAM_ID_ONE);
             Publication publicationTwo = aeron.addPublication(CHANNEL + "|alias=pub-two", STREAM_ID_TWO);
             Subscription subscriptionOne = aeron.addSubscription(CHANNEL + "|alias=sub-one", STREAM_ID_ONE);
             Subscription subscriptionTwo = aeron.addSubscription(CHANNEL + "|alias=sub-two", STREAM_ID_TWO)) {

            CncReader reader = new CncReader(mediaDriver.aeronDirectoryName());

            while (subscriptionOne.hasNoImages()
                    || subscriptionTwo.hasNoImages()
                    || !publicationOne.isConnected()
                    || !publicationTwo.isConnected()) {
                idleStrategy.idle();
            }


            MutableBoolean receivedOne = new MutableBoolean(false);
            MutableBoolean receivedTwo = new MutableBoolean(false);
            FragmentHandler fragmentHandlerOne = (buffer, offset, length, header) -> {
                receivedOne.set(true);
            };
            FragmentHandler fragmentHandlerTwo = (buffer, offset, length, header) -> {
                receivedTwo.set(true);
            };
            UnsafeBuffer buffer = new UnsafeBuffer(new byte[MESSAGE_SIZE]);
            while (publicationOne.offer(buffer) < 0) {
                idleStrategy.idle();
            }

            while (!receivedOne.value && !receivedTwo.value) {
                subscriptionOne.poll(fragmentHandlerOne, Integer.MAX_VALUE);
                subscriptionTwo.poll(fragmentHandlerTwo, Integer.MAX_VALUE);
            }


            CncSnapshot snapshot = reader.read().orElseThrow(AssertionError::new);
            assertEquals(2, snapshot.getStreams().size(), assertionMessage(snapshot));
        }

    }

    private String assertionMessage(CncSnapshot snapshot) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(snapshot);
    }


}
