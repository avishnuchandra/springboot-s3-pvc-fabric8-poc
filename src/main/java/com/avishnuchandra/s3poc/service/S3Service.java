package com.avishnuchandra.s3poc.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.net.URLConnection;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucket;

    public S3Service(S3Client s3Client, @Value("${s3.bucket:app-bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        ensureBucketExists();
    }

    private void ensureBucketExists() {
        try {
            HeadBucketRequest head = HeadBucketRequest.builder().bucket(bucket).build();
            s3Client.headBucket(head);
        } catch (NoSuchBucketException | S3Exception e) {
            CreateBucketRequest req = CreateBucketRequest.builder().bucket(bucket).build();
            s3Client.createBucket(req);
        }
    }

    public void upload(String key, byte[] content, String contentType) {
        PutObjectRequest por = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength((long) content.length)
                .build();
        s3Client.putObject(por, RequestBody.fromBytes(content));
    }

    public byte[] download(String key) {
        GetObjectRequest gor = GetObjectRequest.builder().bucket(bucket).key(key).build();
        try (InputStream in = s3Client.getObject(gor)) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download object " + key, e);
        }
    }

    public String detectContentType(String filename, byte[] content) {
        String ct = null;
        if (filename != null) {
            ct = URLConnection.guessContentTypeFromName(filename);
        }
        if (ct == null) {
            ct = URLConnection.guessContentTypeFromStream(new java.io.ByteArrayInputStream(content));
        }
        return ct == null ? "application/octet-stream" : ct;
    }
}
