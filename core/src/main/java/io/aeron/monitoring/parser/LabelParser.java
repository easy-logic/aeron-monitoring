package io.aeron.monitoring.parser;

public class LabelParser {
    public static void parseStandardLabel(String label, StandardAcceptor acceptor) {
        String[] labelValues = label.split(" ");
        acceptor.accept(
                labelValues[0],
                Long.parseLong(labelValues[1]),
                Integer.parseInt(labelValues[2]),
                Integer.parseInt(labelValues[3]),
                labelValues[4]);
    }

    public static void parsePublisherPosLabel(String label, StandardAcceptor acceptor) {
        String[] labelValues = label.split(" ");
        acceptor.accept(
                labelValues[0] + " " + labelValues[1],
                Long.parseLong(labelValues[2]),
                Integer.parseInt(labelValues[3]),
                Integer.parseInt(labelValues[4]),
                labelValues[5]);
    }

    public static void parseSubscriberPosLabel(String label, SubcriberPosAcceptor acceptor) {
        String[] labelValues = label.split(" ");
        acceptor.accept(
                labelValues[0],
                Long.parseLong(labelValues[1]),
                Integer.parseInt(labelValues[2]),
                Integer.parseInt(labelValues[3]),
                labelValues[4],
                Long.parseLong(labelValues[5].substring(1)));
    }

    @FunctionalInterface
    public interface StandardAcceptor {
        void accept(String name, long registrationId, int sessionId, int streamId, String channel);

    }

    @FunctionalInterface
    public interface SubcriberPosAcceptor {
        void accept(String name, long registrationId, int sessionId, int streamId, String channel, long joinPosition);
    }

}
