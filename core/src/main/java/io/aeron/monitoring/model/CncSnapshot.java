package io.aeron.monitoring.model;

import io.aeron.driver.status.SystemCounterDescriptor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class CncSnapshot {

    private final int version;
    private final int maxCounterId;
    private final Map<SystemCounterDescriptor, CounterValue> counters;
    private final Map<StreamKey, StreamInfo> streams;

}
