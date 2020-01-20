package io.aeron.monitoring.api;

import io.aeron.CncFileDescriptor;
import io.aeron.driver.status.SystemCounterDescriptor;
import io.aeron.monitoring.CncReader;
import io.aeron.monitoring.model.ChannelInfo;
import io.aeron.monitoring.model.CncSnapshot;
import io.aeron.monitoring.model.CounterValue;
import io.aeron.monitoring.model.StreamInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import static io.aeron.CommonContext.AERON_DIR_PROP_DEFAULT;

@RestController
@RequestMapping(value = "/api/v1/cnc", method = RequestMethod.GET)
@Api
public class CncSnapshotController {
    private final CncReader cncReader = new CncReader();

    @RequestMapping("version")
    @ApiOperation("Returns version of the CnC file")
    public int getVersion(@RequestParam("mediaDriver")
                          @ApiParam("Media Driver directory to be read") final Optional<String> mediaDriver) {
        return readSnapshot(mediaDriver).getVersion();
    }

    @RequestMapping("snapshot")
    @ApiOperation("Returns all the information from CnC file at once")
    public CncSnapshot getSnapshot(@RequestParam("mediaDriver")
                                   @ApiParam("Media Driver directory to be read") final Optional<String> mediaDriver) {
        return readSnapshot(mediaDriver);
    }

    @RequestMapping("counters")
    @ApiOperation("Returns counters related to the Media Driver entirely")
    public Map<SystemCounterDescriptor, CounterValue> getCounters(
            @RequestParam("mediaDriver")
            @ApiParam("Media Driver directory to be read") final Optional<String> mediaDriver) {
        return readSnapshot(mediaDriver).getCounters();
    }

    @RequestMapping("counters/{counter}")
    @ApiOperation("Returns single counter value")
    public CounterValue getCounter(@RequestParam("mediaDriver")
                                   @ApiParam("Media Driver directory to be read") final Optional<String> mediaDriver,
                                   @PathVariable("counter")
                                   @ApiParam("Media Driver's counter name") final String counterName) {
        return getCncCounterValue(mediaDriver, counterName);
    }

    @RequestMapping("counters/{counter}/value")
    @ApiOperation("Returns current value of the single counter specified as a parameter")
    public Long getCounterValue(@RequestParam("mediaDriver")
                                @ApiParam("Media Driver directory to be read") final Optional<String> mediaDriver,
                                @PathVariable("counter")
                                @ApiParam("Media Driver's counter name") final String counterName) {
        return getCncCounterValue(mediaDriver, counterName)
                .getValue();
    }

    @RequestMapping("counters/{counter}/label")
    @ApiOperation("Returns human-readable label of the single counter specified as a parameter")
    public String getCounterLabel(@RequestParam("mediaDriver")
                                  @ApiParam("Media Driver directory to be read") final Optional<String> mediaDriver,
                                  @PathVariable("counter")
                                  @ApiParam("Media Driver's counter name") final String counterName) {
        return getCncCounterValue(mediaDriver, counterName).getLabel();
    }

    @RequestMapping("channels")
    @ApiOperation("Return information about all pipes of the Media Driver")
    public Map<String, ChannelInfo> getChannels(@RequestParam("mediaDriver")
                                                @ApiParam("Media Driver directory to be read") final Optional<String> mediaDriver) {
        return readSnapshot(mediaDriver).getChannels();
    }

    @RequestMapping("channels/{channel}")
    @ApiOperation("Return information about single channel specified by parameter")
    public ChannelInfo getChannelInfo(@RequestParam("mediaDriver")
                                      @ApiParam("Media Driver directory to be read") final Optional<String> mediaDriver,
                                      @PathVariable("channel")
                                      @ApiParam("URL which specifies channel") final String channel) {
        return getCncChannelInfo(mediaDriver, channel);
    }

    @RequestMapping("channels/{channel}/{stream}")
    @ApiOperation("Return information about single pipe specified by parameters")
    public StreamInfo getStreamInfo(@RequestParam("mediaDriver")
                                    @ApiParam("Media Driver directory to be read") final Optional<String> mediaDriver,
                                    @PathVariable("channel")
                                    @ApiParam("URL which specifies channel") final String channel,
                                    @PathVariable("streamId")
                                    @ApiParam("Stream identifier") final Integer streamId) {
        final Map<Integer, StreamInfo> streams = getCncChannelInfo(mediaDriver, channel)
                .getStreams();
        return streams.get(streamId);
    }

    private CounterValue getCncCounterValue(final Optional<String> mediaDriver,
                                            final String counterName) {
        final CncSnapshot snapshot = readSnapshot(mediaDriver);
        final SystemCounterDescriptor counter = SystemCounterDescriptor.valueOf(counterName);
        final Map<SystemCounterDescriptor, CounterValue> counters = snapshot.getCounters();
        return counters.get(counter);
    }

    private ChannelInfo getCncChannelInfo(final Optional<String> mediaDriver,
                                          final String channel) {
        final Map<String, ChannelInfo> counters = readSnapshot(mediaDriver).getChannels();
        return counters.get(channel);
    }

    private CncSnapshot readSnapshot(final Optional<String> mediaDriver) {
        final File cnc = new File(mediaDriver.orElse(AERON_DIR_PROP_DEFAULT),
                CncFileDescriptor.CNC_FILE);
        return cncReader.read(cnc);
    }
}
