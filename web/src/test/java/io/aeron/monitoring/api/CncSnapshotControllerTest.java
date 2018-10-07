package io.aeron.monitoring.api;

import static io.aeron.CncFileDescriptor.CNC_VERSION;
import static org.assertj.core.api.Assertions.assertThat;

import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Ignore
public class CncSnapshotControllerTest {

    @Autowired
    private CncSnapshotController controller;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;

    private static MediaDriver.Context context;
    private static MediaDriver mediaDriver;

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
        String mediaDriverDir = mediaDriver.aeronDirectoryName();
        mediaDriverDir = mediaDriverDir.substring(mediaDriverDir.lastIndexOf("/") + 1);;
        String url = "http://localhost:" + port + "/api/v1/cnc/" + mediaDriverDir + "/version";
        ResponseEntity<Integer> ret = testRestTemplate.getForEntity(url, Integer.class);
        assertThat(ret.getBody()).isEqualTo(CNC_VERSION);
    }

    @BeforeClass
    public static void startMediaDriver() {
        context = new MediaDriver.Context();
        context.threadingMode(ThreadingMode.SHARED);
        mediaDriver = MediaDriver.launchEmbedded(context);

    }

    @AfterClass
    public static void stopMediaDriver() {
        mediaDriver.close();
        context.deleteAeronDirectory();
    }
}
