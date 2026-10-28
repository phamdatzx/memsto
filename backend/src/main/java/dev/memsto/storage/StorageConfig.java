package dev.memsto.storage;

import java.net.URI;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

/**
 * One S3 client for every environment: SeaweedFS locally, Cloudflare R2 in production.
 * Path-style addressing works for both.
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    @Bean
    S3Client s3Client(StorageProperties props) {
        return S3Client.builder()
                .endpointOverride(URI.create(props.endpoint()))
                .region(Region.of(props.region()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.accessKey(), props.secretKey())))
                .forcePathStyle(true)
                .build();
    }

    @Bean
    ApplicationRunner createBucketIfMissing(S3Client s3, StorageProperties props) {
        return args -> {
            if (!props.createBucket()) {
                return;
            }
            try {
                s3.headBucket(b -> b.bucket(props.bucket()));
            } catch (NoSuchBucketException e) {
                s3.createBucket(b -> b.bucket(props.bucket()));
            }
        };
    }
}
