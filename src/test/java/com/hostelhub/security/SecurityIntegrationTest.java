package com.hostelhub.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostelhub.dto.CreateComplaintRequest;
import com.hostelhub.dto.RegisterRequest;
import com.hostelhub.dto.UpdatePriorityRequest;
import com.hostelhub.dto.UpdateStatusRequest;
import com.hostelhub.model.ComplaintCategory;
import com.hostelhub.model.ComplaintPriority;
import com.hostelhub.model.ComplaintStatus;
import com.hostelhub.model.Role;
import com.hostelhub.repository.ComplaintRepository;
import com.hostelhub.repository.GroupMembershipRepository;
import com.hostelhub.repository.HostelGroupRepository;
import com.hostelhub.repository.UserRepository;
import com.hostelhub.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HostelGroupRepository groupRepository;

    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtils jwtUtils;

    private String studentToken;
    private String wardenToken;
    private Long wardenUserId;

    @BeforeEach
    void setUp() {
        complaintRepository.deleteAll();
        membershipRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();

        // Register Student
        authService.register(new RegisterRequest("Student One", "student@example.com", "StudentPass123"));
        studentToken = jwtUtils.generateToken("student@example.com", Role.STUDENT.name());

        // Register Warden
        var wardenUser = authService.registerWithRole(new RegisterRequest("Warden One", "warden@example.com", "WardenPass123"), Role.WARDEN);
        wardenUserId = wardenUser.getId();
        wardenToken = jwtUtils.generateToken("warden@example.com", Role.WARDEN.name());
    }

    @Test
    void testUnauthenticatedAccess_Returns401() throws Exception {
        mockMvc.perform(get("/api/complaints"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void testStudentAccess_CreateComplaint_Success() throws Exception {
        CreateComplaintRequest createReq = new CreateComplaintRequest(
                "Geyser Leaking",
                "Hot water leaking in bathroom",
                ComplaintCategory.PLUMBING,
                ComplaintPriority.HIGH,
                "101",
                "Student One"
        );

        mockMvc.perform(post("/api/complaints")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Geyser Leaking"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void testStudentAccess_UpdateStatus_Forbidden403() throws Exception {
        CreateComplaintRequest createReq = new CreateComplaintRequest("Geyser", "Leak", ComplaintCategory.PLUMBING, "101", "Student One");
        MvcResult result = mockMvc.perform(post("/api/complaints")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        long createdId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        UpdateStatusRequest statusReq = new UpdateStatusRequest(ComplaintStatus.IN_PROGRESS);

        // Student attempting to patch status should be forbidden (403)
        mockMvc.perform(patch("/api/complaints/" + createdId + "/status")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void testWardenAccess_AssignAndStatusUpdate_Success() throws Exception {
        CreateComplaintRequest createReq = new CreateComplaintRequest("Geyser", "Leak", ComplaintCategory.PLUMBING, "101", "Student One");
        MvcResult result = mockMvc.perform(post("/api/complaints")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        long createdId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Warden assigns complaint
        mockMvc.perform(patch("/api/complaints/" + createdId + "/assign/" + wardenUserId)
                        .header("Authorization", "Bearer " + wardenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.assignedWardenId").value(wardenUserId));

        // Warden updates priority to URGENT
        UpdatePriorityRequest priorityReq = new UpdatePriorityRequest(ComplaintPriority.URGENT);
        mockMvc.perform(patch("/api/complaints/" + createdId + "/priority")
                        .header("Authorization", "Bearer " + wardenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(priorityReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("URGENT"));

        // Warden updates status to IN_PROGRESS
        UpdateStatusRequest statusReq = new UpdateStatusRequest(ComplaintStatus.IN_PROGRESS);
        mockMvc.perform(patch("/api/complaints/" + createdId + "/status")
                        .header("Authorization", "Bearer " + wardenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }
}
