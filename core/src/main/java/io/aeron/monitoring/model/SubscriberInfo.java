package io.aeron.monitoring.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriberInfo {

    private final long registrationId;
    private final String channel;
    private final int streamId;
    private final long joinPosition;

    private long position;

    public SubscriberInfo(long registrationId, String channel, int streamId, long joinPosition) {
        this.registrationId = registrationId;
        this.channel = channel;
        this.streamId = streamId;
        this.joinPosition = joinPosition;
    }
}
