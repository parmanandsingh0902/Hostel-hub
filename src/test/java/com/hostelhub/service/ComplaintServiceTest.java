package com.hostelhub.service;

import com.hostelhub.dto.*;
import com.hostelhub.exception.InvalidAssignmentException;
import com.hostelhub.exception.InvalidStatusTransitionException;
import com.hostelhub.exception.ResourceNotFoundException;
import com.hostelhub.exception.UnauthorizedComplaintAccessException;
import com.hostelhub.model.*;
import com.hostelhub.repository.ComplaintRepository;
import com.hostelhub.repository.GroupMembershipRepository;
import com.hostelhub.repository.HostelGroupRepository;
import com.hostelhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ComplaintServiceTest {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HostelGroupRepository groupRepository;

    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Autowired
    private ComplaintService service;

    @Autowired
    private AuthService authService;

    @Autowired
    private HostelGroupService groupService;

    @Autowired
    private GroupMembershipService membershipService;

    private UserResponse studentUser;
    private UserResponse wardenUser;
    private GroupResponse group;

    @BeforeEach
    void setUp() {
        complaintRepository.deleteAll();
        membershipRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();

        studentUser = authService.register(new RegisterRequest("Student One", "student1@example.com", "Password123"));
        wardenUser = authService.registerWithRole(new RegisterRequest("Warden One", "warden1@example.com", "Password123"), Role.WARDEN);
        group = groupService.createGroup(new CreateGroupRequest("A Block", "Block A", "A-BLOCK"));

        membershipService.addUserToGroup(group.getId(), studentUser.getId());
    }

    @Test
    void testCreateComplaint_WithStudentAndGroupAssociation() {
        CreateComplaintRequest request = new CreateComplaintRequest(
                "Leaking Tap",
                "Bathroom tap is leaking continuously",
                ComplaintCategory.PLUMBING,
                ComplaintPriority.HIGH,
                "A-102",
                "Student One"
        );

        ComplaintResponse response = service.createComplaint(request, "student1@example.com");

        assertNotNull(response.getId());
        assertEquals("Leaking Tap", response.getTitle());
        assertEquals(ComplaintPriority.HIGH, response.getPriority());
        assertEquals(ComplaintStatus.PENDING, response.getStatus());
        assertEquals(studentUser.getId(), response.getStudentId());
        assertEquals(group.getId(), response.getGroupId());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    void testComplaintStatusWorkflow_PendingToAssignedToInProgressToResolvedToClosed() {
        CreateComplaintRequest request = new CreateComplaintRequest("Tap", "Desc", ComplaintCategory.PLUMBING, "101", "Student One");
        ComplaintResponse created = service.createComplaint(request, "student1@example.com");

        // Assign to Warden -> status ASSIGNED
        ComplaintResponse assigned = service.assignComplaint(created.getId(), wardenUser.getId());
        assertEquals(ComplaintStatus.ASSIGNED, assigned.getStatus());
        assertEquals(wardenUser.getId(), assigned.getAssignedWardenId());

        // Update status to IN_PROGRESS
        ComplaintResponse inProgress = service.updateComplaintStatus(created.getId(), new UpdateStatusRequest(ComplaintStatus.IN_PROGRESS));
        assertEquals(ComplaintStatus.IN_PROGRESS, inProgress.getStatus());

        // Update status to RESOLVED -> populates resolvedAt
        ComplaintResponse resolved = service.updateComplaintStatus(created.getId(), new UpdateStatusRequest(ComplaintStatus.RESOLVED));
        assertEquals(ComplaintStatus.RESOLVED, resolved.getStatus());
        assertNotNull(resolved.getResolvedAt());

        // Update status to CLOSED
        ComplaintResponse closed = service.updateComplaintStatus(created.getId(), new UpdateStatusRequest(ComplaintStatus.CLOSED));
        assertEquals(ComplaintStatus.CLOSED, closed.getStatus());
    }

    @Test
    void testInvalidStatusTransition_ThrowsException() {
        CreateComplaintRequest request = new CreateComplaintRequest("Tap", "Desc", ComplaintCategory.PLUMBING, "101", "Student One");
        ComplaintResponse created = service.createComplaint(request, "student1@example.com");

        // Attempt invalid transition: PENDING -> CLOSED directly
        assertThrows(InvalidStatusTransitionException.class, () ->
                service.updateComplaintStatus(created.getId(), new UpdateStatusRequest(ComplaintStatus.CLOSED)));
    }

    @Test
    void testInvalidWardenAssignment_ThrowsException() {
        CreateComplaintRequest request = new CreateComplaintRequest("Tap", "Desc", ComplaintCategory.PLUMBING, "101", "Student One");
        ComplaintResponse created = service.createComplaint(request, "student1@example.com");

        // Attempt to assign complaint to a STUDENT role user
        assertThrows(InvalidAssignmentException.class, () ->
                service.assignComplaint(created.getId(), studentUser.getId()));
    }

    @Test
    void testUpdatePriority_Success() {
        CreateComplaintRequest request = new CreateComplaintRequest("Tap", "Desc", ComplaintCategory.PLUMBING, "101", "Student One");
        ComplaintResponse created = service.createComplaint(request, "student1@example.com");

        ComplaintResponse updated = service.updateComplaintPriority(created.getId(), new UpdatePriorityRequest(ComplaintPriority.URGENT));
        assertEquals(ComplaintPriority.URGENT, updated.getPriority());
    }

    @Test
    void testStudentAccessIsolation_ViewOtherStudentComplaint_ThrowsException() {
        UserResponse student2 = authService.register(new RegisterRequest("Student Two", "student2@example.com", "Password123"));
        CreateComplaintRequest request = new CreateComplaintRequest("Tap", "Desc", ComplaintCategory.PLUMBING, "101", "Student One");
        ComplaintResponse created = service.createComplaint(request, "student1@example.com");

        // Student 2 attempting to view Student 1 complaint
        assertThrows(UnauthorizedComplaintAccessException.class, () ->
                service.getComplaintByIdForUser(created.getId(), "student2@example.com"));
    }
}
