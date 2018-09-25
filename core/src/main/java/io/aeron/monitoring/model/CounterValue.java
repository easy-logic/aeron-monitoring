package io.aeron.monitoring.model;

import lombok.Builder;
import lombok.Value;
import org.agrona.DirectBuffer;

/**
 * @author Ivan Zemlyanskiy
 */
@Value
@Builder
public class CounterValue {
    int counterId;
    int typeId;
    String label;
    long value;
}
