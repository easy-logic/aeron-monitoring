package io.aeron.monitoring.model;

import lombok.Value;

@Value
public class CounterValue {

    int typeId;
    String label;
    long value;
}
