package io.aeron.monitoring.ext;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.aeron.monitoring.ext.plugins.TestPlugin;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PluginLoaderTest {

    @Autowired
    private PluginLoader pluginLoader;

    @Test(timeout = 10_000)
    public void shouldLoadAndExecutePluginsFromClassPath() {
        final List<Plugin> plugins = pluginLoader.getPlugins();
        assertTrue(!plugins.isEmpty());

        plugins.forEach(p -> {
            final TestPlugin tp = (TestPlugin) p;
            while (!tp.isExecuted()) {
                // NOOP
            }
        });
    }
}
