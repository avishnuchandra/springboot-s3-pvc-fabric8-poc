package com.avishnuchandra.s3poc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.time.Duration;

@Configuration
public class S3Config {

    @Value("${s3.accessKey:}")
    private String accessKey;

    @Value("${s3.secretKey:}")
    private String secretKey;

    @Value("${s3.region:us-east-1}")
    private String region;

    @Value("${s3.endpoint:}")
    private String endpoint;

    @Bean
    public S3Client s3Client() {
        AwsCredentialsProvider credentialsProvider = (accessKey == null || accessKey.isEmpty())
                ? null
                : StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));

        var b = S3Client.builder()
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .apiCallAttemptTimeout(Duration.ofSeconds(60))
                        .build())
                .region(Region.of(region));

        if (credentialsProvider != null) {
            b.credentialsProvider(credentialsProvider);
        }

        if (endpoint != null && !endpoint.isEmpty()) {
            b.endpointOverride(URI.create(endpoint));
            // For S3 compatible services, force path-style access
            b.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }

        return b.build();
    }
}
