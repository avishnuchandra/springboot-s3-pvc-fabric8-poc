package com.avishnuchandra.s3poc.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PvcService {

    private static final Logger log = LoggerFactory.getLogger(PvcService.class);
    private final Path mountPath;

    public PvcService(@Value("${pvc.mountPath:/mnt/data}") String mountPath) {
        this.mountPath = Paths.get(mountPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.mountPath);
        } catch (IOException e) {
            log.warn("Failed to create PVC mount path directory at startup: {}", this.mountPath, e);
        }
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
        Path filepath = resolveFilePath(filename);
        Files.write(filepath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public byte[] readFile(String filename) throws IOException {
        Path filepath = resolveFilePath(filename);
        if (!Files.exists(filepath)) {
            throw new FileNotFoundException("File not found: " + filename);
        }
        return Files.readAllBytes(filepath);
    }

    public void appendFile(String filename, byte[] content) throws IOException {
        Path filepath = resolveFilePath(filename);
        Files.write(filepath, content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public void deleteFile(String filename) throws IOException {
        Path filepath = resolveFilePath(filename);
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

    private Path resolveFilePath(String filename) throws IOException {
        Path resolved = mountPath.resolve(filename).normalize();
        if (!resolved.startsWith(mountPath)) {
            throw new IOException("Invalid filename path: " + filename);
        }
        return resolved;
    }
}
