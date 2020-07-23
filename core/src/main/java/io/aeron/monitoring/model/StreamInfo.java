package io.aeron.monitoring.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public class StreamInfo {

    private final Map<Integer, SessionInfo> sessions = new HashMap<>();


    public Optional<SessionInfo> findSession(int sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }
}
