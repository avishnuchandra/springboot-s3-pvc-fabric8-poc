package com.avishnuchandra.s3poc.controller;

import com.avishnuchandra.s3poc.service.KubernetesService;
import com.avishnuchandra.s3poc.service.PvcService;
import com.avishnuchandra.s3poc.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    private final S3Service s3Service;
    private final PvcService pvcService;
    private final KubernetesService kubernetesService;

    public HealthController(S3Service s3Service, PvcService pvcService, KubernetesService kubernetesService) {
        this.s3Service = s3Service;
        this.pvcService = pvcService;
        this.kubernetesService = kubernetesService;
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> healthAll() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> services = new HashMap<>();

        // S3 Health
        try {
            s3Service.checkHealth();
            services.put("s3", Map.of("status", "UP"));
        } catch (Exception e) {
            services.put("s3", Map.of("status", "DOWN", "error", e.getMessage()));
        }

        // PVC Health
        try {
            pvcService.checkHealth();
            services.put("pvc", Map.of("status", "UP", "mountPath", "/mnt/data"));
        } catch (Exception e) {
            services.put("pvc", Map.of("status", "DOWN", "error", e.getMessage()));
        }

        // Kubernetes Health
        try {
            String namespace = kubernetesService.getCurrentNamespace();
            services.put("kubernetes", Map.of("status", "UP", "namespace", namespace));
        } catch (Exception e) {
            services.put("kubernetes", Map.of("status", "DOWN", "error", e.getMessage()));
        }

        response.put("status", "UP");
        response.put("services", services);

        int downCount = (int) services.values().stream()
                .filter(s -> s instanceof Map)
                .map(s -> (Map<String, Object>) s)
                .filter(s -> "DOWN".equals(s.get("status")))
                .count();

        if (downCount > 0) {
            response.put("status", "PARTIALLY_UP");
            return ResponseEntity.status(503).body(response);
        }

        return ResponseEntity.ok(response);
    }
}
