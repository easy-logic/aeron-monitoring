package io.aeron.monitoring.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("")
public class ReceiverInfo {

    @ApiModelProperty("The status of a receive channel endpoint represented as a counter value")
    private boolean status;

    @ApiModelProperty("The highest position the Receiver has rebuilt up to on a " +
            "session-channel-stream tuple while rebuilding the stream. The stream is complete up " +
            "to this point.")
    private long position;

    @ApiModelProperty("The highest position the Receiver has observed on a " +
            "session-channel-stream tuple while rebuilding the stream. It is possible the stream " +
            "is not complete to this point if the stream has experienced loss.")
    private long highWaterMark;

    public void setStatus(final boolean status) {
        this.status = status;
    }

    public boolean getStatus() {
        return status;
    }

    public void setPosition(final long position) {
        this.position = position;
    }

    public long getPosition() {
        return position;
    }

    public long getHighWaterMark() {
        return highWaterMark;
    }

    public void setHighWaterMark(final long highWaterMark) {
        this.highWaterMark = highWaterMark;
    }
}
