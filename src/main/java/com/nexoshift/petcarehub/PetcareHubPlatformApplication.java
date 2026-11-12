package com.nexoshift.petcarehub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@ConfigurationPropertiesScan
public class PetcareHubPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetcareHubPlatformApplication.class, args);
    }
}