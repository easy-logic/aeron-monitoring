package io.aeron.monitoring.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

@Getter
@RequiredArgsConstructor
public class StreamInfo {

    private final Map<Integer, SessionInfo> sessions = new HashMap<>();
    private final Set<String> aliases = new TreeSet<>();


    public Optional<SessionInfo> findSession(int sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }
}
