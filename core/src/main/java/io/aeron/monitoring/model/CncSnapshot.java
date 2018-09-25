package io.aeron.monitoring.model;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

/**
 * @author Ivan Zemlyanskiy
 */
@Value
@Builder
public class CncSnapshot {
    int version;
    int maxCounterId;
    Map<Integer, CounterValue> counters;
}
