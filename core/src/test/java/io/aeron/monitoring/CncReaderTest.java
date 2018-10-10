package io.aeron.monitoring;

import io.aeron.driver.status.SystemCounterDescriptor;
import io.aeron.monitoring.model.ChannelInfo;
import io.aeron.monitoring.model.CncSnapshot;
import io.aeron.monitoring.model.CounterValue;
import io.aeron.monitoring.model.StreamInfo;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.aeron.driver.status.SystemCounterDescriptor.BYTES_RECEIVED;
import static io.aeron.driver.status.SystemCounterDescriptor.BYTES_SENT;
import static io.aeron.driver.status.SystemCounterDescriptor.CONDUCTOR_PROXY_FAILS;
import static io.aeron.driver.status.SystemCounterDescriptor.CONTROLLABLE_IDLE_STRATEGY;
import static io.aeron.driver.status.SystemCounterDescriptor.ERRORS;
import static io.aeron.driver.status.SystemCounterDescriptor.FLOW_CONTROL_OVER_RUNS;
import static io.aeron.driver.status.SystemCounterDescriptor.FLOW_CONTROL_UNDER_RUNS;
import static io.aeron.driver.status.SystemCounterDescriptor.FREE_FAILS;
import static io.aeron.driver.status.SystemCounterDescriptor.HEARTBEATS_RECEIVED;
import static io.aeron.driver.status.SystemCounterDescriptor.HEARTBEATS_SENT;
import static io.aeron.driver.status.SystemCounterDescriptor.INVALID_PACKETS;
import static io.aeron.driver.status.SystemCounterDescriptor.LOSS_GAP_FILLS;
import static io.aeron.driver.status.SystemCounterDescriptor.NAK_MESSAGES_RECEIVED;
import static io.aeron.driver.status.SystemCounterDescriptor.NAK_MESSAGES_SENT;
import static io.aeron.driver.status.SystemCounterDescriptor.POSSIBLE_TTL_ASYMMETRY;
import static io.aeron.driver.status.SystemCounterDescriptor.RECEIVER_PROXY_FAILS;
import static io.aeron.driver.status.SystemCounterDescriptor.RETRANSMITS_SENT;
import static io.aeron.driver.status.SystemCounterDescriptor.SENDER_FLOW_CONTROL_LIMITS;
import static io.aeron.driver.status.SystemCounterDescriptor.SENDER_PROXY_FAILS;
import static io.aeron.driver.status.SystemCounterDescriptor.SHORT_SENDS;
import static io.aeron.driver.status.SystemCounterDescriptor.STATUS_MESSAGES_RECEIVED;
import static io.aeron.driver.status.SystemCounterDescriptor.STATUS_MESSAGES_SENT;
import static io.aeron.driver.status.SystemCounterDescriptor.UNBLOCKED_COMMANDS;
import static io.aeron.driver.status.SystemCounterDescriptor.UNBLOCKED_PUBLICATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CncReaderTest {


    /**
     * for the test file cnc.dat in test/resources AeronStat util prints this
     * <p>
     * 02:30:43 - Aeron Stat (CnC v14), pid 23553
     * ======================================================================
     * 0:          604,845,376 - Bytes sent
     * 1:          604,845,376 - Bytes received
     * 2:                    0 - Failed offers to ReceiverProxy
     * 3:                    0 - Failed offers to SenderProxy
     * 4:                    0 - Failed offers to DriverConductorProxy
     * 5:                    0 - NAKs sent
     * 6:                    0 - NAKs received
     * 7:               17,937 - Status Messages sent
     * 8:               17,937 - Status Messages received
     * 9:                1,382 - Heartbeats sent
     * 10:               1,382 - Heartbeats received
     * 11:                   0 - Retransmits sent
     * 12:                   0 - Flow control under runs
     * 13:                   0 - Flow control over runs
     * 14:                   0 - Invalid packets
     * 15:                   0 - Errors
     * 16:                   0 - Short sends
     * 17:                   0 - Failed attempts to free log buffers
     * 18:                   2 - Sender flow control limits applied
     * 19:                   0 - Unblocked Publications
     * 20:                   0 - Unblocked Control Commands
     * 21:                   0 - Possible TTL Asymmetry
     * 22:                   0 - ControllableIdleStrategy status
     * 23:                   0 - Loss gap fills
     * 24:                   1 - snd-channel: aeron:udp?endpoint=localhost:40123
     * 25:         302,401,152 - snd-pos: 2 1707220681 10 aeron:udp?endpoint=localhost:40123
     * 26:         302,532,224 - snd-lmt: 2 1707220681 10 aeron:udp?endpoint=localhost:40123
     * 27:         302,401,152 - pub-pos (sample): 2 1707220681 10 aeron:udp?endpoint=localhost:40123
     * 28:         310,789,760 - pub-lmt: 2 1707220681 10 aeron:udp?endpoint=localhost:40123
     * 29:   1,537,918,205,061 - client-heartbeat: 0
     * 30:                   1 - snd-channel: aeron:udp?endpoint=localhost:40124
     * 31:         302,401,152 - snd-pos: 3 1707220682 10 aeron:udp?endpoint=localhost:40124
     * 32:         302,532,224 - snd-lmt: 3 1707220682 10 aeron:udp?endpoint=localhost:40124
     * 33:         302,401,152 - pub-pos (sample): 3 1707220682 10 aeron:udp?endpoint=localhost:40124
     * 34:         310,789,760 - pub-lmt: 3 1707220682 10 aeron:udp?endpoint=localhost:40124
     * 35:   1,537,918,204,917 - client-heartbeat: 1
     * 36:                   1 - rcv-channel: aeron:udp?endpoint=localhost:40124
     * 37:                   1 - rcv-channel: aeron:udp?endpoint=localhost:40123
     * 38:         302,401,152 - sub-pos: 5 1707220681 10 aeron:udp?endpoint=localhost:401 @0st
     * 39:         302,401,152 - rcv-hwm: 6 1707220681 10 aeron:udp?endpoint=localhost:40123
     * 40:         302,401,152 - rcv-pos: 6 1707220681 10 aeron:udp?endpoint=localhost:40123
     * 41:         302,401,152 - sub-pos: 4 1707220682 10 aeron:udp?endpoint=localhost:401 @0st
     * 42:         302,401,152 - rcv-hwm: 7 1707220682 10 aeron:udp?endpoint=localhost:40124
     * 43:         302,401,152 - rcv-pos: 7 1707220682 10 aeron:udp?endpoint=localhost:40124
     */
    @Test
    void read() {
        String testCncFilePath = getClass().getClassLoader().getResource("cnc.dat").getFile();
        CncReader reader = new CncReader();
        CncSnapshot snapshot = reader.read(new File(testCncFilePath));
        assertEquals(14, snapshot.getVersion());
        assertEquals(8192, snapshot.getMaxCounterId());

        final Map<SystemCounterDescriptor, CounterValue> counters =
                snapshot.getCounters()
                        .stream()
                        .collect(Collectors.toMap(
                                CounterValue::getDescriptor,
                                Function.identity()));
        assertEquals(604_845_376L, counters.get(BYTES_SENT).getValue());
        assertEquals(604_845_376L, counters.get(BYTES_RECEIVED).getValue());
        assertEquals(0L, counters.get(RECEIVER_PROXY_FAILS).getValue());
        assertEquals(0L, counters.get(SENDER_PROXY_FAILS).getValue());
        assertEquals(0L, counters.get(CONDUCTOR_PROXY_FAILS).getValue());
        assertEquals(0L, counters.get(NAK_MESSAGES_SENT).getValue());
        assertEquals(0L, counters.get(NAK_MESSAGES_RECEIVED).getValue());
        assertEquals(17_937L, counters.get(STATUS_MESSAGES_SENT).getValue());
        assertEquals(17_937L, counters.get(STATUS_MESSAGES_RECEIVED).getValue());
        assertEquals(1_382L, counters.get(HEARTBEATS_SENT).getValue());
        assertEquals(1_382L, counters.get(HEARTBEATS_RECEIVED).getValue());
        assertEquals(0L, counters.get(RETRANSMITS_SENT).getValue());
        assertEquals(0L, counters.get(FLOW_CONTROL_UNDER_RUNS).getValue());
        assertEquals(0L, counters.get(FLOW_CONTROL_OVER_RUNS).getValue());
        assertEquals(0L, counters.get(INVALID_PACKETS).getValue());
        assertEquals(0L, counters.get(ERRORS).getValue());
        assertEquals(0L, counters.get(SHORT_SENDS).getValue());
        assertEquals(0L, counters.get(FREE_FAILS).getValue());
        assertEquals(2L, counters.get(SENDER_FLOW_CONTROL_LIMITS).getValue());
        assertEquals(0L, counters.get(UNBLOCKED_PUBLICATIONS).getValue());
        assertEquals(0L, counters.get(UNBLOCKED_COMMANDS).getValue());
        assertEquals(0L, counters.get(POSSIBLE_TTL_ASYMMETRY).getValue());
        assertEquals(0L, counters.get(CONTROLLABLE_IDLE_STRATEGY).getValue());
        assertEquals(0L, counters.get(LOSS_GAP_FILLS).getValue());

        final Map<String, ChannelInfo> channels = snapshot.getChannels();
        assertEquals(2, channels.size());
        final ChannelInfo channel1 = channels.get("aeron:udp?endpoint=localhost:40123");
        assertTrue(channel1.getReceiver().getStatus());
        assertEquals(302_401_152L, channel1.getReceiver().getPosition());
        assertEquals(302_401_152L, channel1.getReceiver().getHighWaterMark());
        assertTrue(channel1.getSender().getStatus());
        assertEquals(302_401_152L, channel1.getSender().getPosition());
        assertEquals(302_532_224L, channel1.getSender().getLimit());
        assertEquals(1, channel1.getStreams().size());
        final StreamInfo streamInfo1 = channel1.getStreams().get(10);
        assertEquals(10, streamInfo1.getStreamId());
        assertEquals(302_401_152L, streamInfo1.getPublication().getPosition());
        assertEquals(310_789_760L, streamInfo1.getPublication().getLimit());
        assertEquals(302_401_152L, streamInfo1.getSubscriptions().get(5).getPosition());

        final ChannelInfo channel2 = channels.get("aeron:udp?endpoint=localhost:40124");
        assertTrue(channel2.getReceiver().getStatus());
        assertEquals(302_401_152L, channel2.getReceiver().getPosition());
        assertEquals(302_401_152L, channel2.getReceiver().getHighWaterMark());
        assertTrue(channel2.getSender().getStatus());
        assertEquals(302_401_152L, channel2.getSender().getPosition());
        assertEquals(302_532_224L, channel2.getSender().getLimit());
        assertEquals(1, channel2.getStreams().size());
        final StreamInfo streamInfo2 = channel2.getStreams().get(10);
        assertEquals(10, streamInfo2.getStreamId());
        assertEquals(302_401_152L, streamInfo2.getPublication().getPosition());
        assertEquals(310_789_760L, streamInfo2.getPublication().getLimit());
        assertEquals(302_401_152L, streamInfo2.getSubscriptions().get(4).getPosition());


    }

    @Test
    void shouldParseSampleLabel() {
        assertEquals(
                "aeron:udp?endpoint=localhost:40123",
                CncReader.extractUriFromLabel(
                        "snd-pos: 2 1707220681 10 aeron:udp?endpoint=localhost:40123"));
    }
}
