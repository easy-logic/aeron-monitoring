package io.aeron.monitoring;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AeronMonitoringApplication {

    public static void main(final String[] args) {
        SpringApplication.run(AeronMonitoringApplication.class, args);
    }

    @Bean
    public OpenAPI customOpenAPI(@Value("${app.version}") String appVersion) {
        return new OpenAPI()
                .info(new Info()
                              .title("Aeron Monitoring")
                              .version(appVersion)
                              .description("Read media driver counters"));
    }
}
