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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/cnc", method = RequestMethod.GET)
@Api
public class CncSnapshotController {
    private CncReader cncReader = new CncReader();

    @Value("${aeron.mediaDriver.baseDir:/dev/shm}")
    private String baseDir;

    @RequestMapping("version")
    @ApiOperation("Returns version of the CnC file")
    public int getVersion() {
        return cncReader.read().getVersion();
    }

    @RequestMapping("snapshot")
    @ApiOperation("Returns all the information from CnC file at once")
    public CncSnapshot getSnapshot() {
        return cncReader.read();
    }

    @RequestMapping("counters")
    @ApiOperation("Returns counters related to the Media Driver entirely")
    public Map<SystemCounterDescriptor, CounterValue> getCounters() {
        CncSnapshot snapshot = cncReader.read();
        return snapshot.getCounters();
    }

    @RequestMapping("counters/{counter}")
    @ApiOperation("Returns single counter value")
    public CounterValue getCounter(@PathVariable("counter")
                                   @ApiParam("Media Driver's counter name")
                                   String counterName) {
        CounterValue ret = getCncCounterValue(counterName);
        return ret;
    }

    @RequestMapping("counters/{counter}/value")
    @ApiOperation("Returns current value of the single counter specified as a parameter")
    public Long getCounterValue(@PathVariable("counter")
                                @ApiParam("Media Driver's counter name")
                                String counterName) {
        CounterValue ret = getCncCounterValue(counterName);
        return ret.getValue();
    }

    @RequestMapping("counters/{counter}/label")
    @ApiOperation("Returns human-readable label of the single counter specified as a parameter")
    public String getCounterLabel(@PathVariable("counter")
                                  @ApiParam("Media Driver's counter name")
                                  String counterName) {
        CounterValue ret = getCncCounterValue(counterName);
        return ret.getLabel();
    }

    @RequestMapping("channels")
    @ApiOperation("Return information about all pipes of the Media Driver")
    public Map<String, ChannelInfo> getChannels() {
        CncSnapshot snapshot = cncReader.read();
        return snapshot.getChannels();
    }

    @RequestMapping("channels/{channel}")
    @ApiOperation("Return information about single channel specified by parameter")
    public ChannelInfo getChannelInfo(@PathVariable("channel")
                                      @ApiParam("URL which specifies channel")
                                      String channel) {
        ChannelInfo channelInfo = getCncChannelInfo(channel);
        return channelInfo;
    }

    @RequestMapping("channels/{channel}/{stream}")
    @ApiOperation("Return information about single pipe specified by parameters")
    public StreamInfo getStreamInfo(@PathVariable("channel")
                                    @ApiParam("URL which specifies channel")
                                    String channel,
                                    @PathVariable("streamId")
                                    @ApiParam("Stream identifier")
                                    Integer streamId) {
        ChannelInfo channelInfo = getCncChannelInfo(channel);
        Map<Integer, StreamInfo> streams = channelInfo.getStreams();
        StreamInfo streamInfo = streams.get(streamId);
        return streamInfo;
    }

    private CounterValue getCncCounterValue(String counterName) {
        SystemCounterDescriptor counter = SystemCounterDescriptor.valueOf(counterName);
        CncSnapshot snapshot = cncReader.read();
        Map<SystemCounterDescriptor, CounterValue> counters = snapshot.getCounters();
        return counters.get(counter);
    }

    private ChannelInfo getCncChannelInfo(String channel) {
        CncSnapshot snapshot = cncReader.read();
        Map<String, ChannelInfo> counters = snapshot.getChannels();
        return counters.get(channel);
    }

    @RequestMapping("{mediaDriver}/version")
    @ApiOperation("Returns version of the CnC file")
    public int getVersion(@PathVariable("mediaDriver")
                          @ApiParam("Media Driver directory to be read")
                          String mediaDriver) {
        return readSnapshot(mediaDriver).getVersion();
    }

    @RequestMapping("{mediaDriver}/snapshot")
    @ApiOperation("Returns all the information from CnC file at once")
    public CncSnapshot getSnapshot(@PathVariable("mediaDriver")
                                   @ApiParam("Media Driver directory to be read")
                                   String mediaDriver) {
        return readSnapshot(mediaDriver);
    }

    @RequestMapping("{mediaDriver}/counters")
    @ApiOperation("Returns counters related to the Media Driver entirely")
    public Map<SystemCounterDescriptor, CounterValue> getCounters(
            @PathVariable("mediaDriver")
            @ApiParam("Media Driver directory to be read")
            String mediaDriver) {
        CncSnapshot snapshot = readSnapshot(mediaDriver);
        return snapshot.getCounters();
    }

    @RequestMapping("{mediaDriver}/counters/{counter}")
    @ApiOperation("Returns single counter value")
    public CounterValue getCounter(@PathVariable("mediaDriver")
                                   @ApiParam("Media Driver directory to be read")
                                   String mediaDriver,
                                   @PathVariable("counter")
                                   @ApiParam("Media Driver's counter name")
                                   String counterName) {
        CounterValue ret = getCncCounterValue(mediaDriver, counterName);
        return ret;
    }

    @RequestMapping("{mediaDriver}/counters/{counter}/value")
    @ApiOperation("Returns current value of the single counter specified as a parameter")
    public Long getCounterValue(@PathVariable("mediaDriver")
                                @ApiParam("Media Driver directory to be read")
                                String mediaDriver,
                                @PathVariable("counter")
                                @ApiParam("Media Driver's counter name")
                                String counterName) {
        CounterValue ret = getCncCounterValue(mediaDriver, counterName);
        return ret.getValue();
    }

    @RequestMapping("{mediaDriver}/counters/{counter}/label")
    @ApiOperation("Returns human-readable label of the single counter specified as a parameter")
    public String getCounterLabel(@PathVariable("mediaDriver")
                                  @ApiParam("Media Driver directory to be read")
                                  String mediaDriver,
                                  @PathVariable("counter")
                                  @ApiParam("Media Driver's counter name")
                                  String counterName) {
        CounterValue ret = getCncCounterValue(mediaDriver, counterName);
        return ret.getLabel();
    }

    @RequestMapping("{mediaDriver}/channels")
    @ApiOperation("Return information about all pipes of the Media Driver")
    public Map<String, ChannelInfo> getChannels(@PathVariable("mediaDriver")
                                                @ApiParam("Media Driver directory to be read")
                                                String mediaDriver) {
        CncSnapshot snapshot = readSnapshot(mediaDriver);
        return snapshot.getChannels();
    }

    @RequestMapping("{mediaDriver}/channels/{channel}")
    @ApiOperation("Return information about single channel specified by parameter")
    public ChannelInfo getChannelInfo(@PathVariable("mediaDriver")
                                      @ApiParam("Media Driver directory to be read")
                                      String mediaDriver,
                                      @PathVariable("channel")
                                      @ApiParam("URL which specifies channel")
                                      String channel) {
        ChannelInfo channelInfo = getCncChannelInfo(mediaDriver, channel);
        return channelInfo;
    }

    @RequestMapping("{mediaDriver}/channels/{channel}/{stream}")
    @ApiOperation("Return information about single pipe specified by parameters")
    public StreamInfo getStreamInfo(@PathVariable("mediaDriver")
                                    @ApiParam("Media Driver directory to be read")
                                    String mediaDriver,
                                    @PathVariable("channel")
                                    @ApiParam("URL which specifies channel")
                                    String channel,
                                    @PathVariable("streamId")
                                    @ApiParam("Stream identifier")
                                    Integer streamId) {
        ChannelInfo channelInfo = getCncChannelInfo(channel);
        Map<Integer, StreamInfo> streams = channelInfo.getStreams();
        StreamInfo streamInfo = streams.get(streamId);
        return streamInfo;
    }

    private CounterValue getCncCounterValue(String mediaDriver, String counterName) {
        SystemCounterDescriptor counter = SystemCounterDescriptor.valueOf(counterName);
        CncSnapshot snapshot = cncReader.read();
        Map<SystemCounterDescriptor, CounterValue> counters = snapshot.getCounters();
        return counters.get(counter);
    }

    private ChannelInfo getCncChannelInfo(String mediaDriver, String channel) {
        CncSnapshot snapshot = readSnapshot(mediaDriver);
        Map<String, ChannelInfo> counters = snapshot.getChannels();
        return counters.get(channel);
    }

    private CncSnapshot readSnapshot(String mediaDriver) {
        return cncReader.read(getCncFile(mediaDriver));
    }

    private File getCncFile(String mediaDriver) {
        File base = new File(baseDir);
        File[] shmDirs = base.listFiles(pathname -> pathname.isDirectory());
        if (shmDirs == null) {
            throw new IllegalArgumentException("Directory not found: " + baseDir);
        }

        for (File shmDir : shmDirs) {
            if (shmDir.getName().equals(mediaDriver)) {
                return new File(shmDir, CncFileDescriptor.CNC_FILE);
            }
        }

        throw new IllegalArgumentException("Media driver not found: " + mediaDriver);
    }
}
