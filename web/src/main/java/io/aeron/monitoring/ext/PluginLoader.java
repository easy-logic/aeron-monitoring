package io.aeron.monitoring.ext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

/**
 * Loads {@link Plugin}s on the application startup.
 *
 * <p>Uses {@link ServiceLoader} to find plug-ins in the application classpath
 */
@Component
public class PluginLoader {

    private static final Logger LOG = LoggerFactory.getLogger(PluginLoader.class);

    private final ApplicationArguments applicationArguments;

    private final ThreadPool taskExecutor;

    private final List<Plugin> plugins = new ArrayList<>();

    public PluginLoader(ApplicationArguments applicationArguments, ThreadPool taskExecutor) {
        this.applicationArguments = applicationArguments;
        this.taskExecutor = taskExecutor;
    }

    public List<Plugin> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    /**
     * Loads, initializes and executes plug-ins after the instance is constructed.
     */
    @PostConstruct
    public void init() {
        LOG.info("Loading plugins...");
        final String[] args = applicationArguments.getSourceArgs();
        ServiceLoader.load(Plugin.class).forEach(p -> {
            try {
                p.init(args);
                plugins.add(p);
                taskExecutor.execute(p);
                LOG.info("Loaded: {}", p);
            } catch (final Exception ex) {
                LOG.warn("Pligin initialization error: {}", ex.getMessage());
            }
        });
    }

    /**
     * Shuts down plug-ins before the instance is destroyed.
     */
    @PreDestroy
    public void shutdown() {
        plugins.forEach(p -> {
            try {
                p.shutdown();
            } catch (final Exception ex) {
                LOG.error("Pligin shutdown error: {}", ex.getMessage());
            }
        });
    }
}
