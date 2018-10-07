package io.aeron.monitoring.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class SubscriptionInfo {

    @ApiModelProperty("Subscription ID")
    private final Integer id;

    @ApiModelProperty("The position an individual Subscriber has reached on a " +
            "session-channel-stream tuple. It is possible to have multiple Subscribers on the " +
            "same machine tracked by a MediaDriver")
    private long position;

    public SubscriptionInfo(final Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(final long position) {
        this.position = position;
    }
}
