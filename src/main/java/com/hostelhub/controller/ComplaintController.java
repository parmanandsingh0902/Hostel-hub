package com.hostelhub.controller;

import com.hostelhub.dto.*;
import com.hostelhub.model.ComplaintCategory;
import com.hostelhub.model.ComplaintPriority;
import com.hostelhub.model.ComplaintStatus;
import com.hostelhub.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping
    public ResponseEntity<ComplaintResponse> createComplaint(
            @Valid @RequestBody CreateComplaintRequest request,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        ComplaintResponse created = complaintService.createComplaint(request, userEmail);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ComplaintResponse>> getAllComplaints(
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) ComplaintPriority priority,
            @RequestParam(required = false) ComplaintCategory category,
            @RequestParam(required = false) Long groupId,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        List<ComplaintResponse> complaints = complaintService.getComplaintsForUser(userEmail, status, priority, category, groupId);
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComplaintResponse> getComplaintById(
            @PathVariable Long id,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        ComplaintResponse complaint = complaintService.getComplaintByIdForUser(id, userEmail);
        return ResponseEntity.ok(complaint);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComplaintResponse> updateComplaint(
            @PathVariable Long id,
            @Valid @RequestBody UpdateComplaintRequest request) {
        ComplaintResponse updated = complaintService.updateComplaint(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/assign/{wardenId}")
    public ResponseEntity<ComplaintResponse> assignComplaint(
            @PathVariable Long id,
            @PathVariable Long wardenId) {
        ComplaintResponse assigned = complaintService.assignComplaint(id, wardenId);
        return ResponseEntity.ok(assigned);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ComplaintResponse> updateComplaintStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        ComplaintResponse updated = complaintService.updateComplaintStatus(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/priority")
    public ResponseEntity<ComplaintResponse> updateComplaintPriority(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePriorityRequest request) {
        ComplaintResponse updated = complaintService.updateComplaintPriority(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComplaint(@PathVariable Long id) {
        complaintService.deleteComplaint(id);
        return ResponseEntity.noContent().build();
    }
}
