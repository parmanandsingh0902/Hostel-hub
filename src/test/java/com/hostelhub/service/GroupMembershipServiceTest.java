package com.hostelhub.service;

import com.hostelhub.dto.CreateGroupRequest;
import com.hostelhub.dto.GroupMemberResponse;
import com.hostelhub.dto.GroupResponse;
import com.hostelhub.dto.RegisterRequest;
import com.hostelhub.dto.UserResponse;
import com.hostelhub.exception.MembershipAlreadyExistsException;
import com.hostelhub.exception.MembershipNotFoundException;
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
class GroupMembershipServiceTest {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Autowired
    private HostelGroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HostelGroupService groupService;

    @Autowired
    private AuthService authService;

    @Autowired
    private GroupMembershipService membershipService;

    private Long userId;
    private Long groupId;

    @BeforeEach
    void setUp() {
        complaintRepository.deleteAll();
        membershipRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();

        UserResponse user = authService.register(new RegisterRequest("Student One", "student1@example.com", "Password123"));
        userId = user.getId();

        GroupResponse group = groupService.createGroup(new CreateGroupRequest("C Block", "Block C", "C-BLOCK"));
        groupId = group.getId();
    }

    @Test
    void testAddUserToGroup_Success() {
        GroupMemberResponse member = membershipService.addUserToGroup(groupId, userId);

        assertNotNull(member);
        assertEquals(userId, member.getUserId());
        assertEquals("Student One", member.getName());
        assertNotNull(member.getJoinedAt());
    }

    @Test
    void testAddUserToGroup_Duplicate_ThrowsException() {
        membershipService.addUserToGroup(groupId, userId);

        assertThrows(MembershipAlreadyExistsException.class, () -> membershipService.addUserToGroup(groupId, userId));
    }

    @Test
    void testGetGroupMembers() {
        membershipService.addUserToGroup(groupId, userId);

        List<GroupMemberResponse> members = membershipService.getGroupMembers(groupId);
        assertEquals(1, members.size());
        assertEquals("student1@example.com", members.get(0).getEmail());
    }

    @Test
    void testGetUserGroups() {
        membershipService.addUserToGroup(groupId, userId);

        List<GroupResponse> userGroups = membershipService.getUserGroups(userId);
        assertEquals(1, userGroups.size());
        assertEquals("C-BLOCK", userGroups.get(0).getSectionCode());
    }

    @Test
    void testRemoveUserFromGroup() {
        membershipService.addUserToGroup(groupId, userId);
        membershipService.removeUserFromGroup(groupId, userId);

        List<GroupMemberResponse> members = membershipService.getGroupMembers(groupId);
        assertTrue(members.isEmpty());
    }

    @Test
    void testRemoveUserFromGroup_NotFound_ThrowsException() {
        assertThrows(MembershipNotFoundException.class, () -> membershipService.removeUserFromGroup(groupId, userId));
    }
}
