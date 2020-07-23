package io.aeron.monitoring.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SenderInfo {

    private final long registrationId;
    private final int sessionId;
    private final int streamId;
    private final String channel;

    private boolean status;
    private long position;
    private long limit;
    private long backpressure;

    public SenderInfo(long registrationId, int sessionId, int streamId, String channel) {
        this.registrationId = registrationId;
        this.sessionId = sessionId;
        this.streamId = streamId;
        this.channel = channel;
    }
}
