package io.aeron.monitoring.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashMap;
import java.util.Map;

@ApiModel("")
public class StreamInfo {

    @ApiModelProperty("StreamID of the channel")
    private final int streamId;

    @ApiModelProperty("Publication counters for the current stream ")
    private PublicationInfo publication;

    @ApiModelProperty("All subscriptions registered for the current stream")
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
