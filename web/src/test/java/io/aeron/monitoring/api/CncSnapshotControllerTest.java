package io.aeron.monitoring.api;

import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import org.agrona.concurrent.SleepingMillisIdleStrategy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;

import static io.aeron.CncFileDescriptor.CNC_VERSION;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = CncSnapshotControllerTest.MediaDriverPathInitializer.class)
public class CncSnapshotControllerTest {

    private static MediaDriver.Context context;
    private static MediaDriver mediaDriver;

    @BeforeAll
    public static void startMediaDriver() {
        context = new MediaDriver.Context();
        context.threadingMode(ThreadingMode.SHARED);
        context.sharedIdleStrategy(new SleepingMillisIdleStrategy(1));
        context.dirDeleteOnShutdown(true);
        mediaDriver = MediaDriver.launchEmbedded(context);
    }

    public static class MediaDriverPathInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            String propertyValue =
                    "app.md.path=" + mediaDriver.aeronDirectoryName().replace("\\", "\\\\");
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    configurableApplicationContext, propertyValue);
        }
    }

    @AfterAll
    public static void stopMediaDriver() {
        mediaDriver.close();
        context.deleteAeronDirectory();
    }


    @Autowired
    private CncSnapshotController controller;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;

    @Test
    public void smoke() {
        assertThat(controller).isNotNull();
    }

    @Test
    public void shouldListenOnPort() {
        String url = "http://localhost:" + port;
        String httpResponse = testRestTemplate.getForObject(url, String.class);
        assertThat(httpResponse).isNotBlank();
    }

    @Test
    public void shouldReturnCnCVersion() {
        String url = "http://localhost:" + port + "/api/v1/cnc/version";
        ResponseEntity<Integer> ret = testRestTemplate.getForEntity(url, Integer.class);
        assertThat(ret.getBody()).isEqualTo(CNC_VERSION);
    }


}
