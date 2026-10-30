package com.nexoshift.petcarehub.clinicalexams.infrastructure.storage.s3.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/** Amazon S3 client. Credentials come from AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY. */
@Configuration
@Profile("prod")
public class AmazonS3Configuration {

    @Bean
    public S3Client s3Client(@Value("${clinical-exams.storage.s3.region}") String region) {
        return S3Client.builder().region(Region.of(region)).build();
    }
}