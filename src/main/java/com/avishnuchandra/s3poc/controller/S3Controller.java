package com.avishnuchandra.s3poc.controller;

import com.avishnuchandra.s3poc.service.S3Service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/s3")
public class S3Controller {

    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        try {
            s3Service.checkHealth();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "UP");
            response.put("service", "S3");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "DOWN");
            response.put("service", "S3");
            response.put("error", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }

    @GetMapping("/buckets")
    public ResponseEntity<List<String>> listBuckets() {
        List<String> buckets = s3Service.listBuckets();
        return ResponseEntity.ok(buckets);
    }

    @GetMapping("/files")
    public ResponseEntity<List<String>> listFiles(@RequestParam(required = false) String bucket) {
        List<String> files = s3Service.listObjects(bucket);
        return ResponseEntity.ok(files);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String bucket) throws Exception {
        String key = file.getOriginalFilename();
        byte[] bytes = file.getBytes();
        String contentType = s3Service.detectContentType(key, bytes);
        s3Service.upload(key, bytes, contentType, bucket);
        Map<String, String> response = new HashMap<>();
        response.put("key", key);
        response.put("size", String.valueOf(bytes.length));
        response.put("contentType", contentType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{key}")
    public ResponseEntity<byte[]> download(
            @PathVariable String key,
            @RequestParam(required = false) String bucket) {
        byte[] data = s3Service.download(key, bucket);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @DeleteMapping("/delete/{key}")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable String key,
            @RequestParam(required = false) String bucket) {
        s3Service.delete(key, bucket);
        Map<String, String> response = new HashMap<>();
        response.put("key", key);
        response.put("status", "deleted");
        return ResponseEntity.ok(response);
    }
}
