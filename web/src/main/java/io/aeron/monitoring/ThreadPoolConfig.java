package io.aeron.monitoring;

import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreadPoolConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ThreadPoolConfig.class);

    private static final String DEFAULT_POOL_NAME = "aeron-mon";
    private static final int DEFAULT_MIN_THREADS = 2;
    private static final int DEFAULT_RESERVED_THREADS = -1;
    private static final int DEFAULT_IDLE_TIMEOUT_MILLIS = 60_000;

    @Bean
    public ThreadPool getThreadPool() throws Exception {
        final QueuedThreadPool pool = new QueuedThreadPool();
        pool.setName(DEFAULT_POOL_NAME);
        pool.setMinThreads(DEFAULT_MIN_THREADS);
        pool.setReservedThreads(DEFAULT_RESERVED_THREADS);
        pool.setIdleTimeout(DEFAULT_IDLE_TIMEOUT_MILLIS);
        LOG.debug("Thread pool: {}", pool);

        return pool;
    }

}
