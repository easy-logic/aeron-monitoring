package io.aeron.monitoring;

import io.aeron.CncFileDescriptor;
import io.aeron.CommonContext;
import io.aeron.monitoring.model.CncSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.agrona.DirectBuffer;
import org.agrona.IoUtil;
import org.agrona.concurrent.status.CountersReader;

import java.io.File;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Optional;

import static io.aeron.CncFileDescriptor.*;

@Slf4j
public class CncReader {
    private final File cncFile;

    public CncReader() {
        cncFile = CommonContext.newDefaultCncFile();
    }

    public CncReader(String aeronDirectoryName) {
        cncFile = Paths.get(aeronDirectoryName).resolve(CncFileDescriptor.CNC_FILE).toFile();
    }

    public Optional<CncSnapshot> read() {
        final MappedByteBuffer cncByteBuffer = IoUtil.mapExistingFile(cncFile, "cnc");
        final DirectBuffer cncMetaData = createMetaDataBuffer(cncByteBuffer);
        final int cncVersion = cncMetaData.getInt(cncVersionOffset(0));

        if (cncVersion != CNC_VERSION) {
            log.warn(
                    "Aeron CnC version does not match: version="
                            + cncVersion
                            + " required="
                            + CNC_VERSION);
            return Optional.empty();
        }

        final CountersReader counters =
                new CountersReader(
                        createCountersMetaDataBuffer(cncByteBuffer, cncMetaData),
                        createCountersValuesBuffer(cncByteBuffer, cncMetaData),
                        StandardCharsets.US_ASCII);

        final CounterParser parser = new CounterParser(counters);
        counters.forEach(parser::accept);

        return Optional.of(new CncSnapshot(cncVersion, counters.maxCounterId(), parser.counterValues, parser.channels));
    }
}
