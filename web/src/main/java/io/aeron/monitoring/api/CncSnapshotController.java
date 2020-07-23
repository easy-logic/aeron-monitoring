package io.aeron.monitoring.api;

import io.aeron.CncFileDescriptor;
import io.aeron.driver.status.SystemCounterDescriptor;
import io.aeron.monitoring.CncReader;
import io.aeron.monitoring.model.*;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.aeron.CommonContext.AERON_DIR_PROP_DEFAULT;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@RestController
@RequestMapping(value = "/api/v1/cnc", method = RequestMethod.GET)
//@Api
public class CncSnapshotController {
    private final CncReader cncReader = new CncReader();

    @RequestMapping("version")
    @Operation(summary = "Returns version of the CnC file")
    public Integer getVersion(
            @RequestParam("mediaDriver")
            /*@ApiParam("Media Driver directory to be read")*/ Optional<String> mediaDriver) {
        return readSnapshot(mediaDriver).getVersion();
    }

    @RequestMapping("snapshot")
    @Operation(summary = "Returns all the information from CnC file at once")
    public CncSnapshot getSnapshot(
            @RequestParam("mediaDriver")
            /*@ApiParam("Media Driver directory to be read")*/ Optional<String> mediaDriver) {
        return readSnapshot(mediaDriver);
    }

    @RequestMapping("counters")
    @Operation(summary = "Returns counters related to the Media Driver entirely")
    public Map<SystemCounterDescriptor, CounterValue> getCounters(
            @RequestParam("mediaDriver")
            /*@ApiParam("Media Driver directory to be read")*/ Optional<String> mediaDriver) {
        return readSnapshot(mediaDriver).getCounters();
    }

    @RequestMapping("counters/{counter}")
    @Operation(summary = "Returns single counter value")
    public CounterValue getCounter(
            @RequestParam("mediaDriver")
            /*@ApiParam("Media Driver directory to be read")*/ Optional<String> mediaDriver,
            @PathVariable("counter")
            /*@ApiParam("Media Driver's counter name")*/ String counterName) {
        return getCncCounterValue(mediaDriver, counterName);
    }

    @RequestMapping("counters/{counter}/value")
    @Operation(summary = "Returns current value of the single counter specified as a parameter")
    public Long getCounterValue(
            @RequestParam("mediaDriver")
            /*@ApiParam("Media Driver directory to be read")*/ Optional<String> mediaDriver,
            @PathVariable("counter")
            /*@ApiParam("Media Driver's counter name")*/ String counterName) {
        return getCncCounterValue(mediaDriver, counterName)
                .getValue();
    }


    @RequestMapping("streams")
    @Operation(summary = "Return information about all streams of the Media Driver")
    public Map<StreamKey, StreamInfo> getEndpoints(
            @RequestParam("mediaDriver")
            /*@ApiParam("Media Driver directory to be read")*/ Optional<String> mediaDriver) {
        return readSnapshot(mediaDriver).getStreams();
    }

    @RequestMapping("streams/{endpoint}/{streamId}")
    @Operation(summary = "Return information about a concrete stream")
    public StreamInfo getEndpoint(
            @RequestParam("mediaDriver")
            /*@ApiParam("Media Driver directory to be read")*/ Optional<String> mediaDriver,
            @PathVariable("endpoint")
            /*@ApiParam("Url or IPC")*/ String endpoint,
            @PathVariable("streamId")
            /*@ApiParam("stream id")*/ Integer streamId) {
        StreamInfo streamInfo = readSnapshot(mediaDriver).getStreams().get(new StreamKey(
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
            @RequestParam("mediaDriver")
            /*@ApiParam("Media Driver directory to be read")*/ Optional<String> mediaDriver,
            @PathVariable("endpoint")
            /*@ApiParam("Url or IPC")*/ String endpoint,
            @PathVariable("streamId")
            /*@ApiParam("stream id")*/ Integer streamId,
            @PathVariable("session")
            /*@ApiParam("session id")*/ Integer sessionId) {
        StreamInfo streamInfo = readSnapshot(mediaDriver).getStreams().get(new StreamKey(
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
            @RequestParam("mediaDriver")
            /*@ApiParam("Media Driver directory to be read")*/ Optional<String> mediaDriver,
            @PathVariable("session")
            /*@ApiParam("session id")*/ Integer sessionId) {
        return readSnapshot(mediaDriver).getStreams().values().stream()
                .flatMap(streamInfo -> streamInfo.getSessions().values().stream())
                .filter(sessionInfo -> sessionInfo.getSessionId() == sessionId)
                .collect(Collectors.toList());
    }

    private CounterValue getCncCounterValue(
            Optional<String> mediaDriver,
            String counterName) {
        CncSnapshot snapshot = readSnapshot(mediaDriver);
        SystemCounterDescriptor counter = SystemCounterDescriptor.valueOf(counterName);
        Map<SystemCounterDescriptor, CounterValue> counters = snapshot.getCounters();
        return counters.get(counter);
    }


    private CncSnapshot readSnapshot(Optional<String> mediaDriver) {
        File cnc = new File(
                mediaDriver.orElse(AERON_DIR_PROP_DEFAULT),
                CncFileDescriptor.CNC_FILE);
        return cncReader.read(cnc);
    }

    @ResponseStatus(value= HttpStatus.NOT_FOUND, reason="No such Stream")
    public static class StreamNotFoundException extends RuntimeException {
        // ...
    }
}
