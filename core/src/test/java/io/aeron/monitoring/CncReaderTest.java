package io.aeron.monitoring;

import io.aeron.monitoring.model.CncSnapshot;
import io.aeron.monitoring.model.CounterValue;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
        /*
        String testCncFilePath = getClass().getClassLoader().getResource("cnc.dat").getFile();
        CncReader reader = new CncReader(new File(testCncFilePath));
        CncSnapshot snapshot = reader.read();
        assertEquals(14, snapshot.getVersion());
        assertEquals(8192, snapshot.getMaxCounterId());

        Map<Integer, CounterValue> counters = snapshot.getCounters();
        assertEquals(604_845_376L, counters.get(0).getValue());
        assertEquals(604_845_376L, counters.get(1).getValue());
        assertEquals(0L, counters.get(2).getValue());
        assertEquals(0L, counters.get(3).getValue());
        assertEquals(0L, counters.get(4).getValue());
        assertEquals(0L, counters.get(5).getValue());
        assertEquals(0L, counters.get(6).getValue());
        assertEquals(17_937L, counters.get(7).getValue());
        assertEquals(17_937L, counters.get(8).getValue());
        assertEquals(1_382L, counters.get(9).getValue());
        assertEquals(1_382L, counters.get(10).getValue());
        assertEquals(0L, counters.get(11).getValue());
        assertEquals(0L, counters.get(12).getValue());
        assertEquals(0L, counters.get(13).getValue());
        assertEquals(0L, counters.get(14).getValue());
        assertEquals(0L, counters.get(15).getValue());
        assertEquals(0L, counters.get(16).getValue());
        assertEquals(0L, counters.get(17).getValue());
        assertEquals(2L, counters.get(18).getValue());
        assertEquals(0L, counters.get(19).getValue());
        assertEquals(0L, counters.get(20).getValue());
        assertEquals(0L, counters.get(21).getValue());
        assertEquals(0L, counters.get(22).getValue());
        assertEquals(0L, counters.get(23).getValue());
        assertEquals(1L, counters.get(24).getValue());
        assertEquals(302_401_152L, counters.get(25).getValue());
        assertEquals(302_532_224L, counters.get(26).getValue());
        assertEquals(302_401_152L, counters.get(27).getValue());
        assertEquals(310_789_760L, counters.get(28).getValue());
        assertEquals(1_537_918_205_061L, counters.get(29).getValue());
        assertEquals(1L, counters.get(30).getValue());
        assertEquals(302_401_152L, counters.get(31).getValue());
        assertEquals(302_532_224L, counters.get(32).getValue());
        assertEquals(302_401_152L, counters.get(33).getValue());
        assertEquals(310_789_760L, counters.get(34).getValue());
        assertEquals(1_537_918_204_917L, counters.get(35).getValue());
        assertEquals(1L, counters.get(36).getValue());
        assertEquals(1L, counters.get(37).getValue());
        assertEquals(302_401_152L, counters.get(38).getValue());
        assertEquals(302_401_152L, counters.get(39).getValue());
        assertEquals(302_401_152L, counters.get(40).getValue());
        assertEquals(302_401_152L, counters.get(41).getValue());
        assertEquals(302_401_152L, counters.get(42).getValue());
        assertEquals(302_401_152L, counters.get(43).getValue());
        */


    }
}
