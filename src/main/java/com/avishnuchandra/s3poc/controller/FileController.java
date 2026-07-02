package com.avishnuchandra.s3poc.controller;

import com.avishnuchandra.s3poc.service.S3Service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
public class FileController {

    private final S3Service s3Service;

    public FileController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestPart("file") MultipartFile file) throws Exception {
        String key = file.getOriginalFilename();
        byte[] bytes = file.getBytes();
        String contentType = s3Service.detectContentType(key, bytes);
        s3Service.upload(key, bytes, contentType);
        return ResponseEntity.ok().body(key);
    }

    @GetMapping("/{key}")
    public ResponseEntity<byte[]> download(@PathVariable String key) {
        byte[] data = s3Service.download(key);
        String ct = null;
        ct = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
                .contentType(MediaType.parseMediaType(ct))
                .body(data);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
