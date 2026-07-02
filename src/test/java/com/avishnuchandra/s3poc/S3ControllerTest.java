package com.avishnuchandra.s3poc;

import com.avishnuchandra.s3poc.controller.S3Controller;
import com.avishnuchandra.s3poc.service.S3Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(S3Controller.class)
public class S3ControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    S3Service s3Service;

    @Test
    void listBucketsShouldReturnOk() throws Exception {
        when(s3Service.listBuckets()).thenReturn(Arrays.asList("bucket1", "bucket2"));

        mvc.perform(get("/s3/buckets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("bucket1"))
                .andExpect(jsonPath("$[1]").value("bucket2"));
    }

    @Test
    void uploadFileShouldReturnOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE, "hello".getBytes());
        when(s3Service.detectContentType("hello.txt", "hello".getBytes())).thenReturn("text/plain");

        mvc.perform(multipart("/s3/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("hello.txt"));
    }

    @Test
    void listFilesShouldReturnOk() throws Exception {
        when(s3Service.listObjects(null)).thenReturn(Arrays.asList("file1.txt", "file2.txt"));

        mvc.perform(get("/s3/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("file1.txt"));
    }
}
