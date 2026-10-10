package com.hostelhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostelhub.dto.CreateGroupRequest;
import com.hostelhub.dto.GroupResponse;
import com.hostelhub.dto.RegisterRequest;
import com.hostelhub.dto.UserResponse;
import com.hostelhub.repository.ComplaintRepository;
import com.hostelhub.repository.GroupMembershipRepository;
import com.hostelhub.repository.HostelGroupRepository;
import com.hostelhub.repository.UserRepository;
import com.hostelhub.service.AuthService;
import com.hostelhub.service.HostelGroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HostelGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private HostelGroupRepository groupRepository;

    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HostelGroupService groupService;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void setUp() {
        complaintRepository.deleteAll();
        membershipRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "WARDEN")
    void testCreateGroup_Warden_Success() throws Exception {
        CreateGroupRequest request = new CreateGroupRequest("D Block", "Block D", "D-BLOCK");

        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("D Block"))
                .andExpect(jsonPath("$.sectionCode").value("D-BLOCK"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void testCreateGroup_Student_Forbidden403() throws Exception {
        CreateGroupRequest request = new CreateGroupRequest("D Block", "Block D", "D-BLOCK");

        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void testGetAllGroups_Student_Success() throws Exception {
        mockMvc.perform(get("/api/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "WARDEN")
    void testMembershipAddAndRemove_Success() throws Exception {
        UserResponse user = authService.register(new RegisterRequest("Test Student", "teststudent@example.com", "Password123"));
        GroupResponse group = groupService.createGroup(new CreateGroupRequest("E Block", "Block E", "E-BLOCK"));

        // Add member
        mockMvc.perform(post("/api/groups/" + group.getId() + "/members/" + user.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.email").value("teststudent@example.com"));

        // Get group members
        mockMvc.perform(get("/api/groups/" + group.getId() + "/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(user.getId()));

        // Remove member
        mockMvc.perform(delete("/api/groups/" + group.getId() + "/members/" + user.getId()))
                .andExpect(status().isNoContent());
    }
}
