package com.avishnuchandra.s3poc.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PvcService {

    private static final String DEFAULT_MOUNT_PATH = "/mnt/data";
    private final Path mountPath;

    public PvcService() {
        this.mountPath = initializeMountPath();
    }

    public void checkHealth() throws IOException {
        if (!Files.exists(mountPath)) {
            throw new IOException("Mount path " + mountPath + " does not exist");
        }
        if (!Files.isWritable(mountPath)) {
            throw new IOException("Mount path " + mountPath + " is not writable");
        }
    }

    public void writeFile(String filename, byte[] content) throws IOException {
        Path filepath = mountPath.resolve(filename);
        Files.write(filepath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public byte[] readFile(String filename) throws IOException {
        Path filepath = mountPath.resolve(filename);
        if (!Files.exists(filepath)) {
            throw new FileNotFoundException("File not found: " + filename);
        }
        return Files.readAllBytes(filepath);
    }

    public void appendFile(String filename, byte[] content) throws IOException {
        Path filepath = mountPath.resolve(filename);
        Files.write(filepath, content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public void deleteFile(String filename) throws IOException {
        Path filepath = mountPath.resolve(filename);
        if (!Files.exists(filepath)) {
            throw new FileNotFoundException("File not found: " + filename);
        }
        Files.delete(filepath);
    }

    public List<String> listFiles() throws IOException {
        if (!Files.exists(mountPath)) {
            return List.of();
        }
        try (var stream = Files.list(mountPath)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        }
    }

    private Path initializeMountPath() {
        Path defaultPath = Paths.get(DEFAULT_MOUNT_PATH);
        if (ensureDirectory(defaultPath)) {
            return defaultPath;
        }
        Path fallbackPath = Paths.get(System.getProperty("java.io.tmpdir"), "springboot-s3-pvc");
        ensureDirectory(fallbackPath);
        return fallbackPath;
    }

    private boolean ensureDirectory(Path path) {
        try {
            Files.createDirectories(path);
            return Files.isWritable(path);
        } catch (IOException e) {
            return false;
        }
    }
}
