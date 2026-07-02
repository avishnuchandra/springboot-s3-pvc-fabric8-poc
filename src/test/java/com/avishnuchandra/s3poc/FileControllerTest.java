package com.avishnuchandra.s3poc;

import com.avishnuchandra.s3poc.controller.FileController;
import com.avishnuchandra.s3poc.service.S3Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
public class FileControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    S3Service s3Service;

    @Test
    void uploadEndpointShouldReturnOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE, "hello".getBytes());
        when(s3Service.detectContentType("hello.txt", "hello".getBytes())).thenReturn("text/plain");

        mvc.perform(multipart("/files").file(file))
                .andExpect(status().isOk());
    }
}
