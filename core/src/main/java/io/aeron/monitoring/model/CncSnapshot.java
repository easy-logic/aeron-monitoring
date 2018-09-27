package io.aeron.monitoring.model;

import java.util.Map;

public class CncSnapshot {
    int version;
    int maxCounterId;
    Map<Integer, CounterValue> counters;
}
