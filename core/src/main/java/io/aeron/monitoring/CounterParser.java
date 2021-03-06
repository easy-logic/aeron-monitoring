package io.aeron.monitoring;

import io.aeron.ChannelUri;
import io.aeron.CommonContext;
import io.aeron.driver.status.SystemCounterDescriptor;
import io.aeron.monitoring.model.*;
import io.aeron.monitoring.parser.LabelParser;
import lombok.extern.log4j.Log4j2;
import org.agrona.DirectBuffer;
import org.agrona.Strings;
import org.agrona.concurrent.status.CountersReader;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.aeron.driver.status.PublisherLimit.PUBLISHER_LIMIT_TYPE_ID;
import static io.aeron.driver.status.PublisherPos.PUBLISHER_POS_TYPE_ID;
import static io.aeron.driver.status.ReceiverHwm.RECEIVER_HWM_TYPE_ID;
import static io.aeron.driver.status.ReceiverPos.RECEIVER_POS_TYPE_ID;
import static io.aeron.driver.status.SenderBpe.SENDER_BPE_TYPE_ID;
import static io.aeron.driver.status.SenderLimit.SENDER_LIMIT_TYPE_ID;
import static io.aeron.driver.status.SenderPos.SENDER_POSITION_TYPE_ID;
import static io.aeron.driver.status.SubscriberPos.SUBSCRIBER_POSITION_TYPE_ID;
import static io.aeron.driver.status.SystemCounterDescriptor.SYSTEM_COUNTER_TYPE_ID;

@Log4j2
class CounterParser implements CountersReader.MetaData {

    final Map<SystemCounterDescriptor, CounterValue> counterValues =
            new EnumMap<>(SystemCounterDescriptor.class);
    final Map<StreamKey, StreamInfo> channels =
            new TreeMap<>(
                    Comparator
                            .comparing(StreamKey::getEndpoint)
                            .thenComparing(StreamKey::getStreamId));
    final CountersReader counters;

    public CounterParser(CountersReader counters) {
        this.counters = counters;
    }

    @Override
    public void accept(
            int counterId,
            int typeId,
            DirectBuffer keyBuffer,
            String label) {

        try {

            long value = counters.getCounterValue(counterId);

            if (typeId == SYSTEM_COUNTER_TYPE_ID) {
                SystemCounterDescriptor descriptor = SystemCounterDescriptor.get(counterId);
                counterValues.put(descriptor, new CounterValue(typeId, label, value));
                return;
            }


                switch (typeId) {
                case SENDER_POSITION_TYPE_ID:
                    LabelParser.parseStandardLabel(label, (name, registrationId, sessionId, streamId, channel) -> {
                        SessionInfo sessionInfo = lookupSession(channel, streamId, sessionId);
                        SenderInfo sender = sessionInfo.createIfAbsentSender(
                                name,
                                registrationId,
                                sessionId,
                                streamId,
                                channel);
                        sender.setPosition(value);
                    });
                    break;
                case SENDER_LIMIT_TYPE_ID:
                    LabelParser.parseStandardLabel(label, (name, registrationId, sessionId, streamId, channel) -> {
                        SessionInfo sessionInfo = lookupSession(channel, streamId, sessionId);
                        SenderInfo sender = sessionInfo.createIfAbsentSender(
                                name,
                                registrationId,
                                sessionId,
                                streamId,
                                channel);
                        lookupSession(channel, streamId, sessionId).getSender().setLimit(value);
                    });
                    break;
                case SENDER_BPE_TYPE_ID:
                    LabelParser.parseStandardLabel(label, (name, registrationId, sessionId, streamId, channel) -> {
                        SessionInfo sessionInfo = lookupSession(channel, streamId, sessionId);
                        SenderInfo sender = sessionInfo.createIfAbsentSender(
                                name,
                                registrationId,
                                sessionId,
                                streamId,
                                channel);
                        sender.setBackpressure(value);
                    });
                    break;
//            case SEND_CHANNEL_STATUS_TYPE_ID:
//                SendChannelStatus.parseLabel(label, streamInfo::acceptSendChannelStatus);
//                streamInfo.getSender().setStatus(value == 1);
//                break;
                case RECEIVER_POS_TYPE_ID:
                    LabelParser.parseStandardLabel(label, (name, registrationId, sessionId, streamId, channel) -> {
                        SessionInfo sessionInfo = lookupSession(channel, streamId, sessionId);
                        ReceiverInfo receiver = sessionInfo.createIfAbsentReceiver(
                                name,
                                registrationId,
                                sessionId,
                                streamId,
                                channel);
                        receiver.setPosition(value);
                    });
                    break;
                case RECEIVER_HWM_TYPE_ID:
                    LabelParser.parseStandardLabel(label, (name, registrationId, sessionId, streamId, channel) -> {
                        SessionInfo sessionInfo = lookupSession(channel, streamId, sessionId);
                        ReceiverInfo receiver = sessionInfo.createIfAbsentReceiver(
                                name,
                                registrationId,
                                sessionId,
                                streamId,
                                channel);
                        receiver.setHighWaterMark(value);
                    });
                    break;
//            case RECEIVE_CHANNEL_STATUS_TYPE_ID:
//                ReceiveChannelStatus.parseLabel(label, streamInfo::acceptReceiveChannelStatus);
//                streamInfo.getReceiver().setStatus(value == 1);
//                break;
                case PUBLISHER_POS_TYPE_ID:
                    LabelParser.parsePublisherPosLabel(label, (name, registrationId, sessionId, streamId, channel) -> {
                        PublisherInfo publisherInfo =
                                lookupSession(channel, streamId, sessionId).createIfAbsentPublisher(
                                        registrationId,
                                        streamId,
                                        channel);
                        publisherInfo.setPosition(value);
                    });
                    break;
                case PUBLISHER_LIMIT_TYPE_ID:
                    LabelParser.parseStandardLabel(label, (name, registrationId, sessionId, streamId, channel) -> {
                        PublisherInfo publisherInfo =
                                lookupSession(channel, streamId, sessionId).createIfAbsentPublisher(
                                        registrationId,
                                        streamId,
                                        channel);
                        publisherInfo.setLimit(value);
                    });
                    break;
                case SUBSCRIBER_POSITION_TYPE_ID:
                    LabelParser.parseSubscriberPosLabel(
                            label,
                            (name, registrationId, sessionId, streamId, channel, joinPosition) -> {
                                SubscriberInfo subscriberInfo =
                                        lookupSession(channel, streamId, sessionId).createIfAbsentSubscriber(
                                                registrationId,
                                                streamId,
                                                channel,
                                                joinPosition);
                                subscriberInfo.setPosition(value);
                            });
                    break;
            }
        } catch (Exception e) {
            log.warn("skip label: " + label + " \nreason: " + e.getMessage());
        }
    }

    private SessionInfo lookupSession(String channel, int streamId, int sessionId) {
        ChannelUri channelUri = ChannelUri.parse(channel);
        String endpoint;
        if (channelUri.isIpc()) {
            endpoint = channelUri.media();
        } else {
            endpoint = channelUri.get(CommonContext.ENDPOINT_PARAM_NAME);
        }

        StreamInfo streamInfo = channels.computeIfAbsent(new StreamKey(endpoint, streamId), k -> new StreamInfo());
        SessionInfo sessionInfo = streamInfo.getSessions().computeIfAbsent(sessionId, SessionInfo::new);
        String alias = channelUri.get(CommonContext.ALIAS_PARAM_NAME);
        if (!Strings.isEmpty(alias)) {
            streamInfo.getAliases().add(alias);
        }
        return sessionInfo;
    }

}
