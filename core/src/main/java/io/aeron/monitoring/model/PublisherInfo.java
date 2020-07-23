package io.aeron.monitoring.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublisherInfo {

    private final long registrationId;
    private final String channel;
    private final int streamId;

    private final String note = "This position is a not a real-time value like the other. Updated one per second.";
    private long position;
    private long limit;


    public PublisherInfo(long registrationId, String channel, int streamId) {
        this.registrationId = registrationId;
        this.channel = channel;
        this.streamId = streamId;
    }

}
