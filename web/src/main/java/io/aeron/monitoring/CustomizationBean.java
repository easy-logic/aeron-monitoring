package io.aeron.monitoring;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomizationBean
    implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    @Autowired
    public CustomizationBean(final ApplicationArguments args) {
        if (args.containsOption("port")) {
            final List<String> ports = args.getOptionValues("port");
            System.out.println("Ports: " + ports.get(0));
        }
    }

    @Override
    public void customize(final ConfigurableServletWebServerFactory server) {
        server.setPort(9000);
    }
}
