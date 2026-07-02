package com.avishnuchandra.s3poc.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.net.URLConnection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucket;

    public S3Service(S3Client s3Client, @Value("${s3.bucket:app-bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        ensureBucketExists(this.bucket);
    }

    private void ensureBucketExists(String bucketName) {
        try {
            HeadBucketRequest head = HeadBucketRequest.builder().bucket(bucketName).build();
            s3Client.headBucket(head);
        } catch (NoSuchBucketException | S3Exception e) {
            CreateBucketRequest req = CreateBucketRequest.builder().bucket(bucketName).build();
            s3Client.createBucket(req);
        }
    }

    public void checkHealth() {
        ListBucketsRequest req = ListBucketsRequest.builder().build();
        s3Client.listBuckets(req);
    }

    public List<String> listBuckets() {
        ListBucketsRequest req = ListBucketsRequest.builder().build();
        ListBucketsResponse response = s3Client.listBuckets(req);
        return response.buckets().stream()
                .map(Bucket::name)
                .collect(Collectors.toList());
    }

    public List<String> listObjects(String bucketName) {
        String targetBucket = (bucketName != null && !bucketName.isEmpty()) ? bucketName : bucket;
        ListObjectsV2Request req = ListObjectsV2Request.builder()
                .bucket(targetBucket)
                .build();
        ListObjectsV2Response response = s3Client.listObjectsV2(req);
        return response.contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
    }

    public void upload(String key, byte[] content, String contentType) {
        upload(key, content, contentType, null);
    }

    public void upload(String key, byte[] content, String contentType, String bucketName) {
        String targetBucket = (bucketName != null && !bucketName.isEmpty()) ? bucketName : bucket;
        PutObjectRequest por = PutObjectRequest.builder()
                .bucket(targetBucket)
                .key(key)
                .contentType(contentType)
                .contentLength((long) content.length)
                .build();
        s3Client.putObject(por, RequestBody.fromBytes(content));
    }

    public byte[] download(String key) {
        return download(key, null);
    }

    public byte[] download(String key, String bucketName) {
        String targetBucket = (bucketName != null && !bucketName.isEmpty()) ? bucketName : bucket;
        GetObjectRequest gor = GetObjectRequest.builder().bucket(targetBucket).key(key).build();
        try (InputStream in = s3Client.getObject(gor)) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download object " + key, e);
        }
    }

    public void delete(String key, String bucketName) {
        String targetBucket = (bucketName != null && !bucketName.isEmpty()) ? bucketName : bucket;
        DeleteObjectRequest dor = DeleteObjectRequest.builder()
                .bucket(targetBucket)
                .key(key)
                .build();
        s3Client.deleteObject(dor);
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
