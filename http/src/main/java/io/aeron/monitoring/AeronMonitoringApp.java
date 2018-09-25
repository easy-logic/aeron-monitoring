package io.aeron.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AeronMonitoringApp {

	public static void main(String[] args) {
		SpringApplication.run(AeronMonitoringApp.class, args);
	}
}
