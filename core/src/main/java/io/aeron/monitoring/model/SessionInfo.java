package io.aeron.monitoring.model;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public class SessionInfo {

    private final int sessionId;

    private SenderInfo sender;
    private ReceiverInfo receiver;
    private final Map<Long, PublisherInfo> publishers = new HashMap<>();
    private final Map<Long, SubscriberInfo> subscribers = new HashMap<>();


    public SenderInfo createIfAbsentSender(
            String name,
            long registrationId,
            int sessionId,
            int streamId,
            String channel) {
        if (sender == null) {
            sender = new SenderInfo(registrationId, sessionId, streamId, channel);
        }
        return sender;
    }

    public ReceiverInfo createIfAbsentReceiver(
            String name,
            long registrationId,
            int sessionId,
            int streamId,
            String channel) {
        if (receiver == null) {
            receiver = new ReceiverInfo(registrationId, sessionId, streamId, channel);
        }
        return receiver;
    }


    public PublisherInfo createIfAbsentPublisher(long registrationId, int streamId, String channel) {
        return publishers.computeIfAbsent(
                registrationId,
                id -> new PublisherInfo(registrationId, channel, streamId));

    }

    public SubscriberInfo createIfAbsentSubscriber(
            long registrationId,
            int streamId,
            String channel,
            long joinPosition) {
        return subscribers.computeIfAbsent(
                registrationId,
                id -> new SubscriberInfo(registrationId, channel, streamId, joinPosition));

    }
}
