package ru.ogbozoyan.core.configuration.s3;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    private final S3Properties properties;

    public S3Config(S3Properties properties) {
        this.properties = properties;
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
            .region(properties.getRegion())
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())
                )
            )
            .endpointOverride(URI.create(properties.getS3Endpoint())) // critical for MinIO
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(true) // MinIO requires this
                    .build()
            )
            .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
            .region(properties.getRegion())
            .endpointOverride(URI.create(properties.getS3Endpoint()))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey()))
            )
            .build();
    }

    @Bean(name = "minioS3Presigner")
    public S3Presigner minioS3Presigner() {
        return S3Presigner.builder()
            .region(properties.getRegion())
            .endpointOverride(URI.create(properties.getS3Endpoint()))
            .serviceConfiguration(S3Configuration.builder()
                .pathStyleAccessEnabled(true) // 🔥 CRITICAL FIX
                .build())
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())))
            .build();
    }
}
