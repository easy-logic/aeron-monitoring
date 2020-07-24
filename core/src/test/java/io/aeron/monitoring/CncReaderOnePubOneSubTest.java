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

import static io.aeron.driver.status.SystemCounterDescriptor.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

class CncReaderOnePubOneSubTest {
    public static final String CHANNEL = "aeron:udp?endpoint=localhost:1234";
    public static final int STREAM_ID = 123;
    public static final int MESSAGE_SIZE = 100;
    public static final String PUB_ALIAS = "pub";
    public static final String SUB_ALIAS = "sub";

    private final IdleStrategy idleStrategy = new SleepingMillisIdleStrategy(1);

    @Test
    void shouldReadOnePublisherOneSubscriber() {
        try (MediaDriver mediaDriver = MediaDriver.launchEmbedded(new MediaDriver.Context().dirDeleteOnShutdown(false));
             Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(mediaDriver.aeronDirectoryName()));
             Publication publication = aeron.addPublication(CHANNEL + "|alias=" + PUB_ALIAS, STREAM_ID);
             Subscription subscription = aeron.addSubscription(CHANNEL + "|alias=" + SUB_ALIAS, STREAM_ID)) {
            CncReader reader = new CncReader(mediaDriver.aeronDirectoryName());

            await().untilAsserted(() -> verifySnapshot(reader, publication, subscription));

            while (subscription.hasNoImages()) {
                idleStrategy.idle();
            }

            await().untilAsserted(() -> verifySnapshot(reader, publication, subscription));

            MutableBoolean received = new MutableBoolean(false);
            FragmentHandler fragmentHandler = (buffer, offset, length, header) -> {
                received.set(true);
            };
            UnsafeBuffer buffer = new UnsafeBuffer(new byte[MESSAGE_SIZE]);
            while (publication.offer(buffer) < 0) {
                idleStrategy.idle();
            }

            await().untilAsserted(() -> verifySnapshot(reader, publication, subscription));

            while (!received.value) {
                subscription.poll(fragmentHandler, Integer.MAX_VALUE);
            }

            await().untilAsserted(() -> verifySnapshot(reader, publication, subscription));


            CncSnapshot snapshot = reader.read().orElseThrow(AssertionError::new);
            StreamInfo streamInfo = findStreamInfo(snapshot);
            SessionInfo sessionInfo =
                    streamInfo.findSession(publication.sessionId()).orElseThrow(AssertionError::new);
            assertEquals(publication.position(), sessionInfo.getReceiver().getHighWaterMark());
            assertEquals(publication.position(), sessionInfo.getReceiver().getPosition());
        }

    }

    private void verifySnapshot(CncReader reader, Publication publication, Subscription subscription) {
        CncSnapshot snapshot = reader.read().orElseThrow(IllegalStateException::new);

        assertThat(publication.position())
                .isLessThanOrEqualTo(snapshot.getCounters().get(BYTES_SENT).getValue());

        assertEquals(
                1,
                snapshot.getStreams().size(),
                "must be only 1 channel\n" + snapshot.getStreams().keySet() + "\n");

        StreamInfo streamInfo = findStreamInfo(snapshot);
        assertNotNull(streamInfo);
        assertTrue(streamInfo.getAliases().contains(PUB_ALIAS));
        assertTrue(streamInfo.getAliases().contains(SUB_ALIAS));


        if (!subscription.hasNoImages()) {
            Image image = subscription.imageAtIndex(0);
            assertThat(image.position())
                    .isLessThanOrEqualTo(snapshot.getCounters().get(BYTES_RECEIVED).getValue());

            assertThat(snapshot.getCounters().get(STATUS_MESSAGES_RECEIVED).getValue()).isGreaterThan(0);
            assertThat(snapshot.getCounters().get(STATUS_MESSAGES_SENT).getValue()).isGreaterThan(0);

            SessionInfo sessionInfo = streamInfo.getSessions().values().iterator().next();
            assertEquals(1, streamInfo.getSessions().size());
            assertEquals(publication.sessionId(), sessionInfo.getSessionId());

            ReceiverInfo receiver = sessionInfo.getReceiver();
            assertNotNull(receiver);
            SenderInfo sender = sessionInfo.getSender();
            assertNotNull(sender);

            assertThat(receiver.getRegistrationId()).isNotZero();
            assertThat(sender.getRegistrationId()).isNotZero();
            assertEquals(publication.sessionId(), receiver.getSessionId());
            assertEquals(publication.sessionId(), sender.getSessionId());

            assertThat(sender.getPosition()).isLessThanOrEqualTo(publication.position());
            assertThat(receiver.getPosition()).isGreaterThanOrEqualTo(image.position());

            assertThat(sender.getLimit()).isGreaterThan(publication.position());


            PublisherInfo publisherInfo = sessionInfo.getPublishers().get(publication.registrationId());
            assertEquals(publication.position(), publisherInfo.getPosition());
            assertEquals(publication.positionLimit(), publisherInfo.getLimit());
            assertEquals(publication.channel(), publisherInfo.getChannel());


            SubscriberInfo subscriberInfo = sessionInfo.getSubscribers().get(subscription.registrationId());
            assertEquals(image.position(), subscriberInfo.getPosition());
            assertEquals(image.joinPosition(), subscriberInfo.getJoinPosition());
            assertEquals(subscription.channel(), subscriberInfo.getChannel());
        }

    }

    private StreamInfo findStreamInfo(CncSnapshot snapshot) {
        String endpoint = ChannelUri.parse(CHANNEL).get(CommonContext.ENDPOINT_PARAM_NAME);
        StreamKey key = new StreamKey(endpoint, STREAM_ID);
        return snapshot.getStreams().get(key);
    }

    private String assertionMessage(CncSnapshot snapshot) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(snapshot);
    }


}
