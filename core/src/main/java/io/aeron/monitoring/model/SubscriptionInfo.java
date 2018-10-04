package io.aeron.monitoring.model;

public class SubscriptionInfo {


    private final Integer id;
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
