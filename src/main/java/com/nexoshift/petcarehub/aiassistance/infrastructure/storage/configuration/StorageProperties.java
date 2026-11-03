package com.nexoshift.petcarehub.aiassistance.infrastructure.storage.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** ai.storage.provider = local | s3, and the folder used by the local adapter. */
@ConfigurationProperties(prefix = "ai.storage")
public record StorageProperties(
    @DefaultValue("local") String provider,
    @DefaultValue("uploads") String localDir) {
}
