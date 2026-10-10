package com.hostelhub.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostelhub.dto.CreateComplaintRequest;
import com.hostelhub.dto.UpdateStatusRequest;
import com.hostelhub.model.ComplaintCategory;
import com.hostelhub.model.ComplaintStatus;
import com.hostelhub.repository.ComplaintRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ComplaintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ComplaintRepository complaintRepository;

    @BeforeEach
    void setUp() {
        complaintRepository.deleteAll();
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("HostelHub Complaint Management API"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void testCreateComplaint_Success() throws Exception {
        CreateComplaintRequest request = new CreateComplaintRequest(
                "Fan Not Working",
                "Ceiling fan in room 204 is making strange noise and stopped",
                ComplaintCategory.ELECTRICAL,
                "204",
                "Aman Verma"
        );

        mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Fan Not Working"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void testCreateComplaint_ValidationError() throws Exception {
        CreateComplaintRequest request = new CreateComplaintRequest(
                "", // empty title
                "Description",
                null, // null category
                "204",
                "Aman Verma"
        );

        mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void testGetAllComplaints() throws Exception {
        mockMvc.perform(get("/api/complaints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void testGetComplaintById_NotFound() throws Exception {
        mockMvc.perform(get("/api/complaints/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @WithMockUser(roles = "WARDEN")
    void testPatchStatus_Success() throws Exception {
        CreateComplaintRequest createReq = new CreateComplaintRequest(
                "Water Heater Broken",
                "No hot water in morning",
                ComplaintCategory.PLUMBING,
                "305",
                "Rohan Gupta"
        );

        MvcResult result = mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        long createdId = jsonNode.get("id").asLong();

        UpdateStatusRequest statusReq = new UpdateStatusRequest(ComplaintStatus.IN_PROGRESS);

        mockMvc.perform(patch("/api/complaints/" + createdId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @WithMockUser(roles = "WARDEN")
    void testDeleteComplaint_Success() throws Exception {
        CreateComplaintRequest createReq = new CreateComplaintRequest(
                "Broken Chair",
                "Study chair leg is broken",
                ComplaintCategory.FURNITURE,
                "101",
                "Vikram Singh"
        );

        MvcResult result = mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        long createdId = jsonNode.get("id").asLong();

        mockMvc.perform(delete("/api/complaints/" + createdId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/complaints/" + createdId))
                .andExpect(status().isNotFound());
    }
}
