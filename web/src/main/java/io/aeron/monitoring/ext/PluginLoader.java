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
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class PluginLoader {

    private static final Logger LOG = LoggerFactory.getLogger(PluginLoader.class);

    @SuppressWarnings("unused")
    @Autowired
    private Environment environment;

    @SuppressWarnings("unused")
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ThreadPool taskExecutor;

    private final List<Plugin> plugins = new ArrayList<>();

    public List<Plugin> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    @PostConstruct
    public void init() {
        LOG.info("Loading plugins...");
        ServiceLoader.load(Plugin.class).forEach(p -> {
            plugins.add(p);
            taskExecutor.execute(p);
            LOG.info("Loaded: {}", p);
        });
    }

    @PreDestroy
    public void shutdown() {
        plugins.forEach(p -> p.shutdown());
    }
}
