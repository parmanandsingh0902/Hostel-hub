package com.hostelhub.controller;

import com.hostelhub.dto.GroupResponse;
import com.hostelhub.model.User;
import com.hostelhub.repository.UserRepository;
import com.hostelhub.service.GroupMembershipService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final GroupMembershipService membershipService;
    private final UserRepository userRepository;

    public UserController(GroupMembershipService membershipService, UserRepository userRepository) {
        this.membershipService = membershipService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{userId}/groups")
    public ResponseEntity<List<GroupResponse>> getUserGroups(@PathVariable Long userId) {
        List<GroupResponse> groups = membershipService.getUserGroups(userId);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/me/groups")
    public ResponseEntity<List<GroupResponse>> getMyGroups(Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current authenticated user not found"));

        List<GroupResponse> groups = membershipService.getUserGroups(currentUser.getId());
        return ResponseEntity.ok(groups);
    }
}
