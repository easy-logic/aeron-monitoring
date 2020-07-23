package io.aeron.monitoring.api;

import io.aeron.driver.status.SystemCounterDescriptor;
import io.aeron.monitoring.CncReader;
import io.aeron.monitoring.model.*;
import io.swagger.v3.oas.annotations.Operation;
import org.agrona.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/cnc", method = RequestMethod.GET)
public class CncSnapshotController {
    private final CncReader cncReader;

    public CncSnapshotController(@Value("${app.md.path}") String mediaDriverPath) {
        if (Strings.isEmpty(mediaDriverPath)) {
            cncReader = new CncReader();
        } else {
            cncReader = new CncReader(mediaDriverPath);
        }
    }

    @RequestMapping("version")
    @Operation(summary = "Returns version of the CnC file")
    public Integer getVersion() {
        return cncReader.read().getVersion();
    }

    @RequestMapping("snapshot")
    @Operation(summary = "Returns all the information from CnC file at once")
    public CncSnapshot getSnapshot() {
        return cncReader.read();
    }

    @RequestMapping("counters")
    @Operation(summary = "Returns counters related to the Media Driver entirely")
    public Map<SystemCounterDescriptor, CounterValue> getCounters() {
        return cncReader.read().getCounters();
    }

    @RequestMapping("counters/{counter}")
    @Operation(summary = "Returns single counter value")
    public CounterValue getCounter(@PathVariable("counter") String counterName) {
        return getCncCounterValue(counterName);
    }

    @RequestMapping("counters/{counter}/value")
    @Operation(summary = "Returns current value of the single counter specified as a parameter")
    public Long getCounterValue(@PathVariable("counter") String counterName) {
        return getCncCounterValue(counterName).getValue();
    }


    @RequestMapping("streams")
    @Operation(summary = "Return information about all streams of the Media Driver")
    public Map<StreamKey, StreamInfo> getEndpoints() {
        return cncReader.read().getStreams();
    }

    @RequestMapping("streams/{endpoint}/{streamId}")
    @Operation(summary = "Return information about a concrete stream")
    public StreamInfo getEndpoint(
            @PathVariable("endpoint") String endpoint,
            @PathVariable("streamId") Integer streamId) {
        StreamInfo streamInfo = cncReader.read().getStreams().get(new StreamKey(
                endpoint.toLowerCase(),
                streamId));
        if (streamInfo == null) {
            throw new StreamNotFoundException();
        }
        return streamInfo;
    }


    @RequestMapping("streams/{endpoint}/{streamId}/{session}")
    @Operation(summary = "Return information about a session")
    public SessionInfo getSession(
            @PathVariable("endpoint") String endpoint,
            @PathVariable("streamId") Integer streamId,
            @PathVariable("session") Integer sessionId) {
        StreamInfo streamInfo = cncReader.read().getStreams().get(new StreamKey(
                endpoint.toLowerCase(),
                streamId));
        if (streamInfo == null) {
            throw new StreamNotFoundException();
        }
        return streamInfo.getSessions().get(sessionId);
    }


    @RequestMapping("sessions/{session}")
    @Operation(summary = "Return information about a session")
    public List<SessionInfo> findSession(
            @PathVariable("session") Integer sessionId) {
        return cncReader.read().getStreams().values().stream()
                .flatMap(streamInfo -> streamInfo.getSessions().values().stream())
                .filter(sessionInfo -> sessionInfo.getSessionId() == sessionId)
                .collect(Collectors.toList());
    }

    private CounterValue getCncCounterValue(
            String counterName) {
        CncSnapshot snapshot = cncReader.read();
        SystemCounterDescriptor counter = SystemCounterDescriptor.valueOf(counterName);
        Map<SystemCounterDescriptor, CounterValue> counters = snapshot.getCounters();
        return counters.get(counter);
    }


    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such Stream")
    public static class StreamNotFoundException extends RuntimeException {
        // empty
    }
}
