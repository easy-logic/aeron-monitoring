package io.aeron.monitoring.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class SenderInfo {

    @ApiModelProperty("")
    private boolean status;

    @ApiModelProperty("The position the Sender has reached for sending data to the media on a " +
            "session-channel-stream tuple.")
    private long position;

    @ApiModelProperty
    private long highWaterMark;

    @ApiModelProperty("The limit as a position in bytes applied to publishers on a " +
            "session-channel-stream tuple. Publishers will experience back pressure when this " +
            "position is passed as a means of flow control")
    private long limit;

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

    public long getLimit() {
        return limit;
    }

    public void setLimit(final long limit) {
        this.limit = limit;
    }
}
