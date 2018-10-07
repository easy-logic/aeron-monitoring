package io.aeron.monitoring.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@ApiModel(description = "Contains sender's and receivers' counters and stream stats")
public class ChannelInfo {

    @ApiModelProperty("channel url")
    private final String uri;

    @ApiModelProperty("Media Driver's sender statistics")
    private SenderInfo sender;

    @ApiModelProperty("Media Driver's receiver statistics")
    private ReceiverInfo receiver;

    @ApiModelProperty("Stream statistics of the given channel")
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
