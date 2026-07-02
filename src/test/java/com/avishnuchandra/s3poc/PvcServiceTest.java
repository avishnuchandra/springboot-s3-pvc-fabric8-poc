package com.avishnuchandra.s3poc;

import com.avishnuchandra.s3poc.service.PvcService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class PvcServiceTest {

    @TempDir
    Path tempDir;

    private PvcService pvcService;

    @BeforeEach
    void setup() {
        pvcService = new PvcService(tempDir.toString());
    }

    @Test
    void shouldWriteAndReadFile() throws IOException {
        String filename = "test.txt";
        byte[] content = "Hello PVC".getBytes();

        pvcService.writeFile(filename, content);
        byte[] read = pvcService.readFile(filename);

        assertArrayEquals(content, read);

        // Cleanup
        pvcService.deleteFile(filename);
    }

    @Test
    void shouldAppendToFile() throws IOException {
        String filename = "append-test.txt";
        byte[] initial = "Hello ".getBytes();
        byte[] append = "World".getBytes();

        pvcService.writeFile(filename, initial);
        pvcService.appendFile(filename, append);
        byte[] read = pvcService.readFile(filename);

        assertEquals("Hello World", new String(read));

        // Cleanup
        pvcService.deleteFile(filename);
    }
}
