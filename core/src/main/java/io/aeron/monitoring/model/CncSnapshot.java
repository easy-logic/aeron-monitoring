package io.aeron.monitoring.model;

import io.aeron.driver.status.SystemCounterDescriptor;

import java.util.Map;

public class CncSnapshot {
    private final int version;
    private final int maxCounterId;
    private final Map<SystemCounterDescriptor, CounterValue> counters;
    private final Map<String, ChannelInfo> channels;

    public CncSnapshot(
            final int version,
            final int maxCounterId,
            final Map<SystemCounterDescriptor, CounterValue> counters,
            final Map<String, ChannelInfo> channels) {
        this.version = version;
        this.maxCounterId = maxCounterId;
        this.counters = counters;
        this.channels = channels;
    }

    public int getVersion() {
        return version;
    }

    public int getMaxCounterId() {
        return maxCounterId;
    }

    public Map<SystemCounterDescriptor, CounterValue> getCounters() {
        return counters;
    }

    public Map<String, ChannelInfo> getChannels() {
        return channels;
    }
}
