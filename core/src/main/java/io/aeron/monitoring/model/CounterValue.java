package io.aeron.monitoring.model;

import io.aeron.driver.status.SystemCounterDescriptor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

@ApiModel(description = "Information about single counter including metric's name, " +
        "human-readable label and current value")
public class CounterValue {

    @ApiModelProperty("Counter name")
    SystemCounterDescriptor descriptor;

    @ApiModelProperty("")
    private final int typeId;

    @ApiModelProperty("Human-readable name of the counter")
    private final String label;

    @ApiModelProperty("Current value of the counter")
    private final long value;

    public CounterValue(
            final SystemCounterDescriptor descriptor,
            final int typeId,
            final String label,
            final long value) {
        this.descriptor = descriptor;
        this.typeId = typeId;
        this.label = label;
        this.value = value;
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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CounterValue that = (CounterValue) o;
        return descriptor == that.descriptor;
    }

    @Override
    public int hashCode() {

        return Objects.hash(descriptor);
    }
}
