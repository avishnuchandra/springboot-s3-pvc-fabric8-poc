package com.avishnuchandra.s3poc.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PvcService {

    private final String mountPath;

    public PvcService(@Value("${pvc.mountPath:/mnt/data}") String mountPath) {
        this.mountPath = mountPath;
        // Ensure mount path exists (for local dev, create if not exists)
        Path path = Paths.get(mountPath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                // Path may not be writable in all environments; continue gracefully
            }
        }
    }

    public void checkHealth() throws IOException {
        Path path = Paths.get(mountPath);
        if (!Files.exists(path)) {
            throw new IOException("Mount path " + mountPath + " does not exist");
        }
        if (!Files.isWritable(path)) {
            throw new IOException("Mount path " + mountPath + " is not writable");
        }
    }

    public void writeFile(String filename, byte[] content) throws IOException {
        Path filepath = Paths.get(mountPath, filename);
        Files.write(filepath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public byte[] readFile(String filename) throws IOException {
        Path filepath = Paths.get(mountPath, filename);
        if (!Files.exists(filepath)) {
            throw new FileNotFoundException("File not found: " + filename);
        }
        return Files.readAllBytes(filepath);
    }

    public void appendFile(String filename, byte[] content) throws IOException {
        Path filepath = Paths.get(mountPath, filename);
        Files.write(filepath, content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public void deleteFile(String filename) throws IOException {
        Path filepath = Paths.get(mountPath, filename);
        if (!Files.exists(filepath)) {
            throw new FileNotFoundException("File not found: " + filename);
        }
        Files.delete(filepath);
    }

    public List<String> listFiles() throws IOException {
        Path path = Paths.get(mountPath);
        if (!Files.exists(path)) {
            return List.of();
        }
        try (var stream = Files.list(path)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        }
    }
}
