package io.aeron.monitoring.model;

import lombok.Value;

@Value
public class StreamKey {

    String endpoint;
    int streamId;

}
