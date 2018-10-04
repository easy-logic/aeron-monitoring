package io.aeron.monitoring.model;

import java.util.HashMap;
import java.util.Map;

public class StreamInfo {
    private final int streamId;
    private PublicationInfo publication;
    private Map<Integer, SubscriptionInfo> subscriptions = new HashMap<>();

    public StreamInfo(final int streamId) {
        this.streamId = streamId;
    }

    public int getStreamId() {
        return streamId;
    }

    public PublicationInfo getPublication() {
        return publication;
    }

    public void setPublication(final PublicationInfo publication) {
        this.publication = publication;
    }

    public Map<Integer, SubscriptionInfo> getSubscriptions() {
        return subscriptions;
    }
}
