package io.aeron.monitoring.ext.plugins;

import io.aeron.monitoring.ext.Plugin;

public class TestPlugin implements Plugin {

    private volatile boolean initialized;
    private volatile boolean executed;
    private volatile boolean shutdown;

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isExecuted() {
        return executed;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public void init(final String[] args) {
        initialized = true;
    }

    @Override
    public void run() {
        executed = true;
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }
}
