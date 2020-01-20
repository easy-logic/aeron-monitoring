package io.aeron.monitoring;

import io.aeron.CommonContext;
import io.aeron.driver.status.PublisherLimit;
import io.aeron.driver.status.PublisherPos;
import io.aeron.driver.status.SystemCounterDescriptor;
import io.aeron.monitoring.model.ChannelInfo;
import io.aeron.monitoring.model.CncSnapshot;
import io.aeron.monitoring.model.CounterValue;
import io.aeron.monitoring.model.PublicationInfo;
import io.aeron.monitoring.model.ReceiverInfo;
import io.aeron.monitoring.model.SenderInfo;
import io.aeron.monitoring.model.StreamInfo;
import io.aeron.monitoring.model.SubscriptionInfo;
import org.agrona.DirectBuffer;
import org.agrona.IoUtil;
import org.agrona.concurrent.status.CountersReader;

import java.io.File;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.aeron.CncFileDescriptor.CNC_VERSION;
import static io.aeron.CncFileDescriptor.cncVersionOffset;
import static io.aeron.CncFileDescriptor.createCountersMetaDataBuffer;
import static io.aeron.CncFileDescriptor.createCountersValuesBuffer;
import static io.aeron.CncFileDescriptor.createMetaDataBuffer;
import static io.aeron.driver.status.ClientHeartbeatStatus.CLIENT_HEARTBEAT_TYPE_ID;
import static io.aeron.driver.status.PublisherLimit.PUBLISHER_LIMIT_TYPE_ID;
import static io.aeron.driver.status.PublisherPos.PUBLISHER_POS_TYPE_ID;
import static io.aeron.driver.status.ReceiveChannelStatus.RECEIVE_CHANNEL_STATUS_TYPE_ID;
import static io.aeron.driver.status.ReceiverHwm.RECEIVER_HWM_TYPE_ID;
import static io.aeron.driver.status.ReceiverPos.RECEIVER_POS_TYPE_ID;
import static io.aeron.driver.status.SendChannelStatus.SEND_CHANNEL_STATUS_TYPE_ID;
import static io.aeron.driver.status.SenderLimit.SENDER_LIMIT_TYPE_ID;
import static io.aeron.driver.status.SenderPos.SENDER_POSITION_TYPE_ID;
import static io.aeron.driver.status.SubscriberPos.SUBSCRIBER_POSITION_TYPE_ID;
import static io.aeron.driver.status.SystemCounterDescriptor.SYSTEM_COUNTER_TYPE_ID;

public class CncReader {

    static String extractUriFromLabel(final String label) {
        final Pattern pattern = Pattern.compile(" (aeron:.+).*$");
        final Matcher matcher = pattern.matcher(label);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalStateException("bad label: " + label);
        }
    }

    static Integer extractIdFromLabel(final String label) {
        final Pattern pattern = Pattern.compile("(\\d+) aeron:.+$");
        return extractInteger(label, pattern);
    }

    static Integer extractPubSubIdFromLabel(final String label, final String counterName) {

        final String regex = Pattern.quote(counterName) + ": (\\d+) .*";
        final Pattern pattern = Pattern.compile(regex);
        return extractInteger(label, pattern);
    }

    private static Integer extractInteger(final String label, final Pattern pattern) {
        final Matcher matcher = pattern.matcher(label);
        if (matcher.find()) {
            return Integer.valueOf(matcher.group(1));
        } else {
            throw new IllegalStateException("bad label: " + label);
        }
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
        counters.forEach(parser);

        return new CncSnapshot(
            cncVersion, counters.maxCounterId(), parser.counterValues, parser.channels);
    }

    private class CounterParser implements CountersReader.MetaData {

        final Map<SystemCounterDescriptor, CounterValue> counterValues =
            new EnumMap<>(SystemCounterDescriptor.class);
        final Map<String, ChannelInfo> channels = new HashMap<>();
        private CountersReader counters;

        public CounterParser(final CountersReader counters) {
            this.counters = counters;
        }

        @Override
        public void accept(
            final int counterId,
            final int typeId,
            final DirectBuffer keyBuffer,
            final String label) {

            final long value = counters.getCounterValue(counterId);

            if (typeId == SYSTEM_COUNTER_TYPE_ID) {
                counterValues.put(
                    SystemCounterDescriptor.get(counterId),
                    new CounterValue(
                        SystemCounterDescriptor.get(counterId), typeId, label, value));
                return;
            }

            if (typeId == CLIENT_HEARTBEAT_TYPE_ID) {
                // skip it for a while
                return;
            }

            final String uri = extractUriFromLabel(label);
            final ChannelInfo channelInfo = channels.computeIfAbsent(uri, ChannelInfo::new);

            switch (typeId) {
                case SENDER_POSITION_TYPE_ID:
                    if (channelInfo.getSender() == null) {
                        channelInfo.setSender(new SenderInfo());
                    }
                    channelInfo.getSender().setPosition(value);
                    break;
                case SENDER_LIMIT_TYPE_ID:
                    if (channelInfo.getSender() == null) {
                        channelInfo.setSender(new SenderInfo());
                    }
                    channelInfo.getSender().setLimit(value);
                    break;
                case SEND_CHANNEL_STATUS_TYPE_ID:
                    if (channelInfo.getSender() == null) {
                        channelInfo.setSender(new SenderInfo());
                    }
                    channelInfo.getSender().setStatus(value == 1);
                    break;
                case RECEIVER_POS_TYPE_ID:
                    if (channelInfo.getReceiver() == null) {
                        channelInfo.setReceiver(new ReceiverInfo());
                    }
                    channelInfo.getReceiver().setPosition(value);
                    break;
                case RECEIVER_HWM_TYPE_ID:
                    if (channelInfo.getReceiver() == null) {
                        channelInfo.setReceiver(new ReceiverInfo());
                    }
                    channelInfo.getReceiver().setHighWaterMark(value);
                    break;
                case RECEIVE_CHANNEL_STATUS_TYPE_ID:
                    if (channelInfo.getReceiver() == null) {
                        channelInfo.setReceiver(new ReceiverInfo());
                    }
                    channelInfo.getReceiver().setStatus(value == 1);
                    break;
                case PUBLISHER_POS_TYPE_ID:
                    updatePublisherPosition(label, value, channelInfo);
                    break;
                case PUBLISHER_LIMIT_TYPE_ID:
                    updatePublisherLimit(label, value, channelInfo);
                    break;
                case SUBSCRIBER_POSITION_TYPE_ID:
                    updateSubscriberPosition(label, value, channelInfo);
                    break;
                default:
                    System.out.println("un");
                    break;
            }
        }

        private void updatePublisherPosition(
            final String label, final long value, final ChannelInfo channelInfo) {
            final StreamInfo streamInfo = findStreamInfo(label, channelInfo);
            if (streamInfo.getPublication() == null) {
                final Integer publisherId = extractPubSubIdFromLabel(label, PublisherPos.NAME);
                streamInfo.setPublication(new PublicationInfo(publisherId));
            }
            streamInfo.getPublication().setPosition(value);
        }

        private void updatePublisherLimit(
            final String label, final long value, final ChannelInfo channelInfo) {
            final StreamInfo streamInfo = findStreamInfo(label, channelInfo);
            if (streamInfo.getPublication() == null) {
                final Integer publisherId = extractPubSubIdFromLabel(label, PublisherPos.NAME);
                streamInfo.setPublication(new PublicationInfo(publisherId));
            }
            streamInfo.getPublication().setLimit(value);
        }

        private void updateSubscriberPosition(
            final String label, final long value, final ChannelInfo channelInfo) {
            final Integer subscriptionId = extractPubSubIdFromLabel(label, PublisherLimit.NAME);

            final SubscriptionInfo subscriptionInfo =
                findStreamInfo(label, channelInfo)
                    .getSubscriptions()
                    .computeIfAbsent(subscriptionId, SubscriptionInfo::new);

            subscriptionInfo.setPosition(value);
        }

        private StreamInfo findStreamInfo(final String label, final ChannelInfo channelInfo) {

            final Integer id = extractIdFromLabel(label);
            return channelInfo.getStreams().computeIfAbsent(id, StreamInfo::new);
        }
    }
}
