package io.aeron.monitoring.model;

import io.aeron.driver.status.SystemCounterDescriptor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

@ApiModel(description = "Information about single counter including metric's name, " +
        "human-readable label and current value")
public class CounterValue implements Comparable<CounterValue> {

    @ApiModelProperty("Counter id in CnC file")
    private final int id;

    @ApiModelProperty("Counter name")
    SystemCounterDescriptor descriptor;

    @ApiModelProperty("Type id of counter")
    private final int typeId;

    @ApiModelProperty("Human-readable name of the counter")
    private final String label;

    @ApiModelProperty("Current value of the counter")
    private final long value;

    public CounterValue(
            final int id,
            final SystemCounterDescriptor descriptor,
            final int typeId,
            final String label,
            final long value) {
        this.id = id;
        this.descriptor = descriptor;
        this.typeId = typeId;
        this.label = label;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public SystemCounterDescriptor getDescriptor() {
        return descriptor;
    }

    public int getTypeId() {
        return typeId;
    }

    public String getLabel() {
        return label;
    }

    public long getValue() {
        return value;
    }

    @Override
    public int compareTo(final CounterValue other) {
        return Integer.compare(id, other.id);
    }
}
