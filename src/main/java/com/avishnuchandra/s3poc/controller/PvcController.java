package com.avishnuchandra.s3poc.controller;

import com.avishnuchandra.s3poc.service.PvcService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pvc")
public class PvcController {

    private final PvcService pvcService;

    public PvcController(PvcService pvcService) {
        this.pvcService = pvcService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        try {
            pvcService.checkHealth();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "UP");
            response.put("service", "PVC");
            response.put("mountPath", "/mnt/data");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "DOWN");
            response.put("service", "PVC");
            response.put("error", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }

    @PostMapping(value = "/write", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> write(
            @RequestPart("file") MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        byte[] bytes = file.getBytes();
        pvcService.writeFile(filename, bytes);
        Map<String, String> response = new HashMap<>();
        response.put("filename", filename);
        response.put("size", String.valueOf(bytes.length));
        response.put("path", "/mnt/data/" + filename);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/read/{filename}")
    public ResponseEntity<byte[]> read(@PathVariable String filename) {
        byte[] data = pvcService.readFile(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @GetMapping("/files")
    public ResponseEntity<List<String>> listFiles() {
        List<String> files = pvcService.listFiles();
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/delete/{filename}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String filename) {
        pvcService.deleteFile(filename);
        Map<String, String> response = new HashMap<>();
        response.put("filename", filename);
        response.put("status", "deleted");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/append/{filename}")
    public ResponseEntity<Map<String, String>> append(
            @PathVariable String filename,
            @RequestPart("file") MultipartFile file) throws Exception {
        byte[] bytes = file.getBytes();
        pvcService.appendFile(filename, bytes);
        Map<String, String> response = new HashMap<>();
        response.put("filename", filename);
        response.put("appended", String.valueOf(bytes.length));
        return ResponseEntity.ok(response);
    }
}
