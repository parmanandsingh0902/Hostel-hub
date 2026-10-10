package com.hostelhub.dto;

import com.hostelhub.model.Role;
import java.time.LocalDateTime;

public class GroupMemberResponse {
    private Long userId;
    private String name;
    private String email;
    private Role role;
    private LocalDateTime joinedAt;

    public GroupMemberResponse() {
    }

    public GroupMemberResponse(Long userId, String name, String email, Role role, LocalDateTime joinedAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
