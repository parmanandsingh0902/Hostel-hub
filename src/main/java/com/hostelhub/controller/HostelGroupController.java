package com.hostelhub.controller;

import com.hostelhub.dto.CreateGroupRequest;
import com.hostelhub.dto.GroupMemberResponse;
import com.hostelhub.dto.GroupResponse;
import com.hostelhub.dto.UpdateGroupRequest;
import com.hostelhub.service.GroupMembershipService;
import com.hostelhub.service.HostelGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class HostelGroupController {

    private final HostelGroupService groupService;
    private final GroupMembershipService membershipService;

    public HostelGroupController(HostelGroupService groupService, GroupMembershipService membershipService) {
        this.groupService = groupService;
        this.membershipService = membershipService;
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getAllGroups() {
        List<GroupResponse> groups = groupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getGroupById(@PathVariable Long id) {
        GroupResponse group = groupService.getGroupById(id);
        return ResponseEntity.ok(group);
    }

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        GroupResponse created = groupService.createGroup(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupResponse> updateGroup(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGroupRequest request) {
        GroupResponse updated = groupService.updateGroup(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    // --- Membership Endpoints ---

    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<GroupMemberResponse> addUserToGroup(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        GroupMemberResponse member = membershipService.addUserToGroup(groupId, userId);
        return new ResponseEntity<>(member, HttpStatus.CREATED);
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeUserFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        membershipService.removeUserFromGroup(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(@PathVariable Long groupId) {
        List<GroupMemberResponse> members = membershipService.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }
}
