package io.aeron.monitoring.ext.plugins;

import io.aeron.monitoring.ext.Plugin;

public class TestPlugin1 implements Plugin, TestPlugin {

    private volatile boolean executed;

    @Override
    public boolean isExecuted() {
        return executed;
    }

    @Override
    public void init() {
    }

    @Override
    public void run() {
        executed = true;
    }

    @Override
    public void shutdown() {
    }
}
