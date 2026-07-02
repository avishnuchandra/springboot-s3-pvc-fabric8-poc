package com.avishnuchandra.s3poc;

import com.avishnuchandra.s3poc.service.S3Service;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Testcontainers(disabledWithoutDocker = true)
public class S3IntegrationTest {

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:2.2"))
            .withServices(LocalStackContainer.Service.S3);

    @Test
    void canUploadAndDownload() throws Exception {
        String endpoint = localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString();
        String region = localstack.getRegion();
        String accessKey = localstack.getAccessKey();
        String secretKey = localstack.getSecretKey();

        S3Client client = S3Client.builder()
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();

        S3Service service = new S3Service(client, "test-bucket");
        String key = "greeting.txt";
        byte[] bytes = "hello localstack".getBytes();
        service.upload(key, bytes, "text/plain");
        byte[] got = service.download(key);
        Assertions.assertArrayEquals(bytes, got);
    }
}
