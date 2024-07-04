package net.briclabs.evcoordinator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ApiApp {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApiApp.class);

    public static void main(String[] args) {
        LOGGER.info("=================== Starting EV Coordinator API application. ===================");
        SpringApplication.run(ApiApp.class, args);
        LOGGER.info("=================== Started EV Coordinator API application. ===================");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
