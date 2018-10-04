package io.aeron.monitoring.ext;

public interface Plugin extends Runnable {

    void init();
    
    void shutdown();

}
