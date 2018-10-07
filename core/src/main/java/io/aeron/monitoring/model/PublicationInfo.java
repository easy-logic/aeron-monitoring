package io.aeron.monitoring.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "")
public class PublicationInfo {

    @ApiModelProperty("Publisher ID")
    private final Integer id;

    @ApiModelProperty("The position in bytes a publication has reached appending to the log. " +
            "This is a not a real-time value like the other and is updated one per second for " +
            "monitoring purposes.")
    private long position;

    @ApiModelProperty("The limit as a position in bytes applied to publishers on a " +
            "session-channel-stream tuple. Publishers will experience back pressure when this " +
            "position is passed as a means of flow control")
    private long limit;

    public PublicationInfo(final Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setPosition(final long position) {
        this.position = position;
    }

    public long getPosition() {
        return position;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(final long limit) {
        this.limit = limit;
    }
}
