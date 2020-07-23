package io.aeron.monitoring;

import io.aeron.ChannelUri;
import io.aeron.CommonContext;
import io.aeron.driver.status.SystemCounterDescriptor;
import io.aeron.monitoring.model.*;
import io.aeron.monitoring.parser.LabelParser;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.status.CountersReader;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.aeron.driver.status.ClientHeartbeatTimestamp.CLIENT_HEARTBEAT_TYPE_ID;
import static io.aeron.driver.status.PublisherLimit.PUBLISHER_LIMIT_TYPE_ID;
import static io.aeron.driver.status.PublisherPos.PUBLISHER_POS_TYPE_ID;
import static io.aeron.driver.status.ReceiveChannelStatus.RECEIVE_CHANNEL_STATUS_TYPE_ID;
import static io.aeron.driver.status.ReceiverHwm.RECEIVER_HWM_TYPE_ID;
import static io.aeron.driver.status.ReceiverPos.RECEIVER_POS_TYPE_ID;
import static io.aeron.driver.status.SendChannelStatus.SEND_CHANNEL_STATUS_TYPE_ID;
import static io.aeron.driver.status.SenderBpe.SENDER_BPE_TYPE_ID;
import static io.aeron.driver.status.SenderLimit.SENDER_LIMIT_TYPE_ID;
import static io.aeron.driver.status.SenderPos.SENDER_POSITION_TYPE_ID;
import static io.aeron.driver.status.SubscriberPos.SUBSCRIBER_POSITION_TYPE_ID;
import static io.aeron.driver.status.SystemCounterDescriptor.SYSTEM_COUNTER_TYPE_ID;
import static io.aeron.status.LocalSocketAddressStatus.LOCAL_SOCKET_ADDRESS_STATUS_TYPE_ID;

class CounterParser implements CountersReader.MetaData {

    final Map<SystemCounterDescriptor, CounterValue> counterValues =
            new EnumMap<>(SystemCounterDescriptor.class);
    final Map<StreamKey, StreamInfo> channels = new TreeMap<>(Comparator.comparing(StreamKey::getEndpoint));
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

        long value = counters.getCounterValue(counterId);

        if (typeId == SYSTEM_COUNTER_TYPE_ID) {
            SystemCounterDescriptor descriptor = SystemCounterDescriptor.get(counterId);
            counterValues.put(descriptor, new CounterValue(typeId, label, value));
            return;
        }

        if (Set.of(
                CLIENT_HEARTBEAT_TYPE_ID,
                SEND_CHANNEL_STATUS_TYPE_ID,
                RECEIVE_CHANNEL_STATUS_TYPE_ID,
                LOCAL_SOCKET_ADDRESS_STATUS_TYPE_ID
        ).contains(typeId)) {
            return;
        }

        StreamKey key = extractStreamKeyFromLabel(label);
        StreamInfo streamInfo = channels.computeIfAbsent(key, k -> new StreamInfo());
        switch (typeId) {
            case SENDER_POSITION_TYPE_ID -> {
                LabelParser.parseStandardLabel(label, (name, registrationId, sessionId, streamId, channel) -> {
                    SessionInfo sessionInfo = lookupSession(streamInfo, sessionId);
                    SenderInfo sender = sessionInfo.createIfAbsentSender(
                            name,
                            registrationId,
                            sessionId,
                            streamId,
                            channel);
                    sender.setPosition(value);
                });
            }
            case SENDER_LIMIT_TYPE_ID -> {
                LabelParser.parseStandardLabel(label, (name, registrationId, sessionId, streamId, channel) -> {
                    SessionInfo sessionInfo = lookupSession(streamInfo, sessionId);
                    SenderInfo sender = sessionInfo.createIfAbsentSender(
                            name,
                            registrationId,
                            sessionId,
                            streamId,
                            channel);
                    lookupSession(streamInfo, sessionId).getSender().setLimit(value);
                });
            }
            case SENDER_BPE_TYPE_ID -> {
                LabelParser.parseStandardLabel(label, (name, registrationId, sessionId, streamId, channel) -> {
                    SessionInfo sessionInfo = lookupSession(streamInfo, sessionId);
                    SenderInfo sender = sessionInfo.createIfAbsentSender(
                            name,
                            registrationId,
                            sessionId,
                            streamId,
                            channel);
                    sender.setBackpressure(value);
                });
            }
//            case SEND_CHANNEL_STATUS_TYPE_ID -> {
//                SendChannelStatus.parseLabel(label, streamInfo::acceptSendChannelStatus);
//                streamInfo.getSender().setStatus(value == 1);
//            }
            case RECEIVER_POS_TYPE_ID -> {
                LabelParser.parseStandardLabel(label, (name, registrationId, sessionId, streamId, channel) -> {
                    SessionInfo sessionInfo = lookupSession(streamInfo, sessionId);
                    ReceiverInfo receiver = sessionInfo.createIfAbsentReceiver(
                            name,
                            registrationId,
                            sessionId,
                            streamId,
                            channel);
                    receiver.setPosition(value);
                });
            }
            case RECEIVER_HWM_TYPE_ID -> {
                LabelParser.parseStandardLabel(label, (name, registrationId, sessionId, streamId, channel) -> {
                    SessionInfo sessionInfo = lookupSession(streamInfo, sessionId);
                    ReceiverInfo receiver = sessionInfo.createIfAbsentReceiver(
                            name,
                            registrationId,
                            sessionId,
                            streamId,
                            channel);
                    receiver.setHighWaterMark(value);
                });
            }
//            case RECEIVE_CHANNEL_STATUS_TYPE_ID -> {
//                ReceiveChannelStatus.parseLabel(label, streamInfo::acceptReceiveChannelStatus);
//                streamInfo.getReceiver().setStatus(value == 1);
//            }
            case PUBLISHER_POS_TYPE_ID -> {
                LabelParser.parsePublisherPosLabel(label, (name, registrationId, sessionId, streamId, channel) -> {
                    PublisherInfo publisherInfo =
                            lookupSession(streamInfo, sessionId).createIfAbsentPublisher(
                                    registrationId,
                                    streamId,
                                    channel);
                    publisherInfo.setPosition(value);
                });
            }
            case PUBLISHER_LIMIT_TYPE_ID -> {
                LabelParser.parseStandardLabel(label, (name, registrationId, sessionId, streamId, channel) -> {
                    PublisherInfo publisherInfo =
                            lookupSession(streamInfo, sessionId).createIfAbsentPublisher(
                                    registrationId,
                                    streamId,
                                    channel);
                    publisherInfo.setLimit(value);
                });
            }
            case SUBSCRIBER_POSITION_TYPE_ID -> {
                LabelParser.parseSubscriberPosLabel(
                        label,
                        (name, registrationId, sessionId, streamId, channel, joinPosition) -> {
                            SubscriberInfo subscriberInfo = lookupSession(streamInfo, sessionId)
                                    .createIfAbsentSubscriber(
                                            registrationId,
                                            streamId,
                                            channel,
                                            joinPosition);
                            subscriberInfo.setPosition(value);
                        });
            }
        }
    }

    private SessionInfo lookupSession(StreamInfo streamInfo, int sessionId) {
        return streamInfo.getSessions().computeIfAbsent(sessionId, SessionInfo::new);
    }

    static StreamKey extractStreamKeyFromLabel(String label) {
        Pattern pattern = Pattern.compile("(\\d+) +(aeron:\\P{Blank}+).*$");
        Matcher matcher = pattern.matcher(label);
        if (!matcher.find()) {
            throw new IllegalStateException("bad label: " + label);
        }

        String uri = matcher.group(2);
        ChannelUri channelUri = ChannelUri.parse(uri);
        String endpoint;
        if (channelUri.isIpc()) {
            endpoint = channelUri.media();
        } else {
            endpoint = channelUri.get(CommonContext.ENDPOINT_PARAM_NAME);
        }
        return new StreamKey(endpoint, Integer.parseInt(matcher.group(1)));
    }
}
