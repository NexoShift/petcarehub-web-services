package com.nexoshift.petcarehub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PetcareHubPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetcareHubPlatformApplication.class, args);
    }
}