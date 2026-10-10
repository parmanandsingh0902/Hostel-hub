package com.hostelhub.dto;

import com.hostelhub.model.ComplaintCategory;
import com.hostelhub.model.ComplaintPriority;
import com.hostelhub.model.ComplaintStatus;

import java.time.LocalDateTime;

public class ComplaintResponse {
    private Long id;
    private String title;
    private String description;
    private ComplaintCategory category;
    private ComplaintPriority priority;
    private String roomNumber;
    private String studentName;
    private Long studentId;
    private String studentEmail;
    private Long groupId;
    private String groupName;
    private String groupSectionCode;
    private Long assignedWardenId;
    private String assignedWardenName;
    private ComplaintStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    public ComplaintResponse() {
    }

    public ComplaintResponse(Long id, String title, String description, ComplaintCategory category,
                             String roomNumber, String studentName, ComplaintStatus status,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = ComplaintPriority.MEDIUM;
        this.roomNumber = roomNumber;
        this.studentName = studentName;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public ComplaintResponse(Long id, String title, String description, ComplaintCategory category,
                             ComplaintPriority priority, String roomNumber, String studentName,
                             Long studentId, String studentEmail, Long groupId, String groupName,
                             String groupSectionCode, Long assignedWardenId, String assignedWardenName,
                             ComplaintStatus status, LocalDateTime createdAt, LocalDateTime updatedAt,
                             LocalDateTime resolvedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.roomNumber = roomNumber;
        this.studentName = studentName;
        this.studentId = studentId;
        this.studentEmail = studentEmail;
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupSectionCode = groupSectionCode;
        this.assignedWardenId = assignedWardenId;
        this.assignedWardenName = assignedWardenName;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.resolvedAt = resolvedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ComplaintCategory getCategory() {
        return category;
    }

    public void setCategory(ComplaintCategory category) {
        this.category = category;
    }

    public ComplaintPriority getPriority() {
        return priority;
    }

    public void setPriority(ComplaintPriority priority) {
        this.priority = priority;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupSectionCode() {
        return groupSectionCode;
    }

    public void setGroupSectionCode(String groupSectionCode) {
        this.groupSectionCode = groupSectionCode;
    }

    public Long getAssignedWardenId() {
        return assignedWardenId;
    }

    public void setAssignedWardenId(Long assignedWardenId) {
        this.assignedWardenId = assignedWardenId;
    }

    public String getAssignedWardenName() {
        return assignedWardenName;
    }

    public void setAssignedWardenName(String assignedWardenName) {
        this.assignedWardenName = assignedWardenName;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
