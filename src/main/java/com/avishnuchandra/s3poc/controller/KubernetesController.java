package com.avishnuchandra.s3poc.controller;

import com.avishnuchandra.s3poc.service.KubernetesService;
import io.fabric8.kubernetes.api.model.Job;
import io.fabric8.kubernetes.api.model.Pod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/k8s")
public class KubernetesController {

    private final KubernetesService kubernetesService;

    public KubernetesController(KubernetesService kubernetesService) {
        this.kubernetesService = kubernetesService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        try {
            String namespace = kubernetesService.getCurrentNamespace();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "UP");
            response.put("service", "Kubernetes");
            response.put("namespace", namespace);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "DOWN");
            response.put("service", "Kubernetes");
            response.put("error", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }

    @GetMapping("/pods")
    public ResponseEntity<List<String>> listPods(@RequestParam(required = false) String namespace) {
        List<String> pods = kubernetesService.listPods(namespace);
        return ResponseEntity.ok(pods);
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<String>> listJobs(@RequestParam(required = false) String namespace) {
        List<String> jobs = kubernetesService.listJobs(namespace);
        return ResponseEntity.ok(jobs);
    }

    @PostMapping("/jobs")
    public ResponseEntity<Map<String, String>> createJob(
            @RequestBody JobRequest jobRequest,
            @RequestParam(required = false) String namespace) {
        String jobName = kubernetesService.createJob(jobRequest, namespace);
        Map<String, String> response = new HashMap<>();
        response.put("jobName", jobName);
        response.put("status", "created");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/jobs/{name}")
    public ResponseEntity<Map<String, String>> deleteJob(
            @PathVariable String name,
            @RequestParam(required = false) String namespace) {
        kubernetesService.deleteJob(name, namespace);
        Map<String, String> response = new HashMap<>();
        response.put("jobName", name);
        response.put("status", "deleted");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/namespace")
    public ResponseEntity<Map<String, String>> getCurrentNamespace() {
        String namespace = kubernetesService.getCurrentNamespace();
        Map<String, String> response = new HashMap<>();
        response.put("namespace", namespace);
        return ResponseEntity.ok(response);
    }

    public static class JobRequest {
        public String name;
        public String image;
        public String[] command;
        public Map<String, String> env;

        public JobRequest() {}

        public JobRequest(String name, String image, String[] command) {
            this.name = name;
            this.image = image;
            this.command = command;
        }
    }
}
