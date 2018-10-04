package io.aeron.monitoring.model;

public class ReceiverInfo {

    private boolean status;
    private long position;
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
