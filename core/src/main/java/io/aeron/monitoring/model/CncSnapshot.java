package io.aeron.monitoring.model;

import io.aeron.driver.status.SystemCounterDescriptor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

@ApiModel(description = "Represents the entire snapshot of Aeron's CnC file")
public class CncSnapshot {

    @ApiModelProperty("Version of CNC file")
    private final int version;

    @ApiModelProperty("Maximum counter id which can be supported given the length of the values buffer")
    private final int maxCounterId;

    @ApiModelProperty("Contains counters related to media driver entirely")
    private final Map<SystemCounterDescriptor, CounterValue> counters;

    @ApiModelProperty("Contains information related to media driver's pipes")
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
