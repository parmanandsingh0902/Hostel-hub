package com.hostelhub.service;

import com.hostelhub.dto.CreateGroupRequest;
import com.hostelhub.dto.GroupResponse;
import com.hostelhub.dto.UpdateGroupRequest;
import com.hostelhub.exception.GroupAlreadyExistsException;
import com.hostelhub.exception.GroupNotFoundException;
import com.hostelhub.repository.ComplaintRepository;
import com.hostelhub.repository.GroupMembershipRepository;
import com.hostelhub.repository.HostelGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HostelGroupServiceTest {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private HostelGroupRepository groupRepository;

    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Autowired
    private HostelGroupService groupService;

    @BeforeEach
    void setUp() {
        complaintRepository.deleteAll();
        membershipRepository.deleteAll();
        groupRepository.deleteAll();
    }

    @Test
    void testCreateGroup_Success() {
        CreateGroupRequest request = new CreateGroupRequest("A Block", "Boys Hostel Block A", "A-BLOCK");

        GroupResponse response = groupService.createGroup(request);

        assertNotNull(response.getId());
        assertEquals("A Block", response.getName());
        assertEquals("Boys Hostel Block A", response.getDescription());
        assertEquals("A-BLOCK", response.getSectionCode());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    void testCreateGroup_DuplicateSectionCode_ThrowsException() {
        CreateGroupRequest req1 = new CreateGroupRequest("A Block", "Block A", "A-BLOCK");
        groupService.createGroup(req1);

        CreateGroupRequest req2 = new CreateGroupRequest("Another Block", "Block A Duplicate", "A-BLOCK");
        assertThrows(GroupAlreadyExistsException.class, () -> groupService.createGroup(req2));
    }

    @Test
    void testGetAllGroups() {
        groupService.createGroup(new CreateGroupRequest("A Block", "Block A", "A-BLOCK"));
        groupService.createGroup(new CreateGroupRequest("B Block", "Block B", "B-BLOCK"));

        List<GroupResponse> groups = groupService.getAllGroups();
        assertEquals(2, groups.size());
    }

    @Test
    void testGetGroupById_Success() {
        GroupResponse created = groupService.createGroup(new CreateGroupRequest("A Block", "Block A", "A-BLOCK"));
        GroupResponse found = groupService.getGroupById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("A Block", found.getName());
    }

    @Test
    void testGetGroupById_NotFound() {
        assertThrows(GroupNotFoundException.class, () -> groupService.getGroupById(999L));
    }

    @Test
    void testUpdateGroup() {
        GroupResponse created = groupService.createGroup(new CreateGroupRequest("A Block", "Block A", "A-BLOCK"));

        UpdateGroupRequest updateReq = new UpdateGroupRequest("A Block Renamed", "Updated desc", "A-BLOCK-UPD");
        GroupResponse updated = groupService.updateGroup(created.getId(), updateReq);

        assertEquals("A Block Renamed", updated.getName());
        assertEquals("A-BLOCK-UPD", updated.getSectionCode());
    }

    @Test
    void testDeleteGroup() {
        GroupResponse created = groupService.createGroup(new CreateGroupRequest("A Block", "Block A", "A-BLOCK"));

        groupService.deleteGroup(created.getId());

        assertThrows(GroupNotFoundException.class, () -> groupService.getGroupById(created.getId()));
    }
}
