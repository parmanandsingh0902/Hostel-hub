package com.hostelhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostelhub.dto.LoginRequest;
import com.hostelhub.dto.RegisterRequest;
import com.hostelhub.repository.ComplaintRepository;
import com.hostelhub.repository.GroupMembershipRepository;
import com.hostelhub.repository.HostelGroupRepository;
import com.hostelhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Autowired
    private HostelGroupRepository groupRepository;

    @BeforeEach
    void setUp() {
        complaintRepository.deleteAll();
        membershipRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testRegister_Success() throws Exception {
        RegisterRequest request = new RegisterRequest("Amit Kumar", "amit@example.com", "Password@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Amit Kumar"))
                .andExpect(jsonPath("$.email").value("amit@example.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.password").doesNotExist()); // Ensure password is NEVER returned
    }

    @Test
    void testRegister_DuplicateEmail_Conflict() throws Exception {
        RegisterRequest request = new RegisterRequest("Amit Kumar", "amit@example.com", "Password@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void testLogin_Success() throws Exception {
        RegisterRequest regRequest = new RegisterRequest("Amit Kumar", "amit@example.com", "Password@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("amit@example.com", "Password@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("amit@example.com"))
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    void testLogin_InvalidCredentials_Unauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest("nonexistent@example.com", "WrongPass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }
}
