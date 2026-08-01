package com.kirithika.studentadmission.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kirithika.studentadmission.dto.request.RegisterRequest;
import com.kirithika.studentadmission.enums.Gender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3306/student_admission_db",
        "spring.datasource.username=root",
        "spring.datasource.password=root123",
        "jwt.secret=your-256-bit-secret-key-here-make-it-long-and-random-enough",
        "jwt.expiration=86400000",
        "spring.data.redis.host=touching-coyote-151984.upstash.io",
        "spring.data.redis.port=6379",
        "spring.data.redis.password=gQAAAAAAAlGwAAIgcDIxNDA2NWI5OTQzNmY0NjU1OGU3MDA5Y2VjZDZiOTY2Ng",
        "spring.data.redis.ssl.enabled=true",
        "razorpay.key.id=rzp_test_TIBrW8Q1Claav1",
        "razorpay.key.secret=nBCUkhw2KU83wvIuK7Io61SU"
})

class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_shouldReturn201AndToken_whenRequestIsValid() throws Exception {

        RegisterRequest request = RegisterRequest.builder()
                .fullName("Integration Test User")
                .email("integrationtest_" + UUID.randomUUID() + "@example.com")
                .password("password123")
                .phoneNumber("9" + System.currentTimeMillis() % 1000000000L)
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .gender(Gender.OTHER)
                .address("Test Address")
                .city("Test City")
                .state("Test State")
                .country("India")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void register_shouldReturn400_whenEmailIsInvalid() throws Exception {

        RegisterRequest request = RegisterRequest.builder()
                .fullName("Test User")
                .email("not-a-valid-email")
                .password("password123")
                .phoneNumber("9999999999")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}