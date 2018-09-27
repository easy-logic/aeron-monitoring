package io.aeron.monitoring;

import io.aeron.CommonContext;
import io.aeron.monitoring.model.CncSnapshot;
import io.aeron.monitoring.model.CounterValue;
import org.agrona.DirectBuffer;
import org.agrona.IoUtil;
import org.agrona.concurrent.status.CountersReader;

import java.io.File;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static io.aeron.CncFileDescriptor.*;

public class CncReader {

    private final int cncVersion;
    private final CountersReader counters;

    public CncReader() {
        this(CommonContext.newDefaultCncFile());
    }

    public CncReader(File cncFile) {
        System.out.println("Command `n Control file " + cncFile);

        final MappedByteBuffer cncByteBuffer = IoUtil.mapExistingFile(cncFile, "cnc");
        final DirectBuffer cncMetaData = createMetaDataBuffer(cncByteBuffer);
        cncVersion = cncMetaData.getInt(cncVersionOffset(0));

        if (cncVersion != CNC_VERSION) {
            throw new IllegalStateException(
                    "Aeron CnC version does not match: version=" + cncVersion + " required=" + CNC_VERSION);
        }

        counters = new CountersReader(
                createCountersMetaDataBuffer(cncByteBuffer, cncMetaData),
                createCountersValuesBuffer(cncByteBuffer, cncMetaData),
                StandardCharsets.US_ASCII);
    }

    public CncSnapshot read() {
        /*
        Map<Integer, CounterValue> counterValues = new HashMap<>();
        counters.forEach((counterId, typeId, keyBuffer, label) -> {
            counterValues.put(
                    counterId, 
                    CounterValue.builder()
                            .counterId(counterId)
                            .typeId(typeId)
                            .label(label)
                            .value(counters.getCounterValue(counterId))
                            .build());
        });


        return CncSnapshot.builder()
                .version(cncVersion)
                .maxCounterId(counters.maxCounterId())
                .counters(counterValues)
                .build();
                */
        return null;
    }


}
