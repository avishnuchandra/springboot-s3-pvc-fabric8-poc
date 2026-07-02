package com.avishnuchandra.s3poc.service;

import com.avishnuchandra.s3poc.controller.KubernetesController;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KubernetesService {

    private final KubernetesClient kubernetesClient;

    public KubernetesService() {
        this.kubernetesClient = new KubernetesClientBuilder().build();
    }

    public String getCurrentNamespace() {
        String namespace = kubernetesClient.getNamespace();
        return namespace != null ? namespace : "default";
    }

    public List<String> listPods(String namespace) {
        String ns = (namespace != null && !namespace.isEmpty()) ? namespace : getCurrentNamespace();
        return kubernetesClient.pods()
                .inNamespace(ns)
                .list()
                .getItems()
                .stream()
                .map(pod -> pod.getMetadata().getName())
                .collect(Collectors.toList());
    }

    public List<String> listJobs(String namespace) {
        String ns = (namespace != null && !namespace.isEmpty()) ? namespace : getCurrentNamespace();
        return kubernetesClient.batch()
                .v1()
                .jobs()
                .inNamespace(ns)
                .list()
                .getItems()
                .stream()
                .map(job -> job.getMetadata().getName())
                .collect(Collectors.toList());
    }

    public String createJob(KubernetesController.JobRequest jobRequest, String namespace) {
        String ns = (namespace != null && !namespace.isEmpty()) ? namespace : getCurrentNamespace();

        Map<String, String> labels = new HashMap<>();
        labels.put("app", jobRequest.name);

        Container container = new ContainerBuilder()
                .withName(jobRequest.name + "-container")
                .withImage(jobRequest.image)
                .withCommand(jobRequest.command != null ? List.of(jobRequest.command) : null)
                .withEnv(buildEnvVars(jobRequest.env))
                .build();

        PodTemplateSpec podTemplate = new PodTemplateSpecBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withLabels(labels)
                        .build())
                .withSpec(new PodSpecBuilder()
                        .withContainers(container)
                        .withRestartPolicy("Never")
                        .build())
                .build();

        Job job = new io.fabric8.kubernetes.api.model.JobBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(jobRequest.name)
                        .withNamespace(ns)
                        .build())
                .withSpec(new io.fabric8.kubernetes.api.model.JobSpecBuilder()
                        .withTemplate(podTemplate)
                        .withBackoffLimit(3)
                        .build())
                .build();

        io.fabric8.kubernetes.api.model.Job created = kubernetesClient.batch()
                .v1()
                .jobs()
                .inNamespace(ns)
                .create(job);

        return created.getMetadata().getName();
    }

    public void deleteJob(String name, String namespace) {
        String ns = (namespace != null && !namespace.isEmpty()) ? namespace : getCurrentNamespace();
        kubernetesClient.batch()
                .v1()
                .jobs()
                .inNamespace(ns)
                .withName(name)
                .delete();
    }

    private List<EnvVar> buildEnvVars(Map<String, String> envMap) {
        if (envMap == null || envMap.isEmpty()) {
            return List.of();
        }
        return envMap.entrySet().stream()
                .map(e -> new EnvVarBuilder()
                        .withName(e.getKey())
                        .withValue(e.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}
