package io.aeron.monitoring.model;

public class PublicationInfo {

    private final Integer id;
    private long position;
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
