package io.aeron.monitoring.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChannelInfo {
    private final String uri;
    private SenderInfo sender;
    private ReceiverInfo receiver;
    private final Map<Integer, StreamInfo> streams = new HashMap<>();

    public ChannelInfo(final String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public SenderInfo getSender() {
        return sender;
    }

    public void setSender(final SenderInfo sender) {
        this.sender = sender;
    }

    public ReceiverInfo getReceiver() {
        return receiver;
    }

    public void setReceiver(final ReceiverInfo receiver) {
        this.receiver = receiver;
    }

    public Map<Integer, StreamInfo> getStreams() {
        return streams;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ChannelInfo that = (ChannelInfo)o;
        return Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }
}
