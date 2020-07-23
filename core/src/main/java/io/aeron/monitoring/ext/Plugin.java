package io.aeron.monitoring.ext;

/*
 * Defines Plug-in.
 *
 * Plug-in is an optional task running in a separate thread within a thread pool,
 */
public interface Plugin extends Runnable {

    /**
     * Method called ones before the plug-in is dispatched to the thread pool.
     *
     * @param args Raw application arguments: exactly how the were passed to the
     *             application
     */
    void init(final String[] args);

    /**
     * Method called ones before the application thread pool is stopped.
     */
    void shutdown();
}
