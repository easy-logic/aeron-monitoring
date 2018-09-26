package io.aeron.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AeronMonitoringApplication {

    public static void main(final String[] args) {
        SpringApplication.run(AeronMonitoringApplication.class, args);
    }
}
