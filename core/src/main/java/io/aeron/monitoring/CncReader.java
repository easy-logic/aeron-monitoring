package io.aeron.monitoring;

import io.aeron.CncFileDescriptor;
import io.aeron.CommonContext;
import io.aeron.monitoring.model.CncSnapshot;
import org.agrona.DirectBuffer;
import org.agrona.IoUtil;
import org.agrona.concurrent.status.CountersReader;

import java.io.File;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static io.aeron.CncFileDescriptor.*;

public class CncReader {

    public CncSnapshot lookupAndRead(String aeronDirectoryName) {
        return read(Paths.get(aeronDirectoryName).resolve(CncFileDescriptor.CNC_FILE).toFile());
    }

    public CncSnapshot read() {
        return read(CommonContext.newDefaultCncFile());
    }

    public CncSnapshot read(final File cncFile) {
        final MappedByteBuffer cncByteBuffer = IoUtil.mapExistingFile(cncFile, "cnc");
        final DirectBuffer cncMetaData = createMetaDataBuffer(cncByteBuffer);
        final int cncVersion = cncMetaData.getInt(cncVersionOffset(0));

        if (cncVersion != CNC_VERSION) {
            throw new IllegalStateException(
                    "Aeron CnC version does not match: version="
                            + cncVersion
                            + " required="
                            + CNC_VERSION);
        }

        final CountersReader counters =
                new CountersReader(
                        createCountersMetaDataBuffer(cncByteBuffer, cncMetaData),
                        createCountersValuesBuffer(cncByteBuffer, cncMetaData),
                        StandardCharsets.US_ASCII);

        final CounterParser parser = new CounterParser(counters);
        counters.forEach(parser::accept);

        return new CncSnapshot(
                cncVersion, counters.maxCounterId(), parser.counterValues, parser.channels);
    }

}
