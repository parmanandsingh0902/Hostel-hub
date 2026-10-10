package com.hostelhub.service;

import com.hostelhub.dto.*;
import com.hostelhub.exception.*;
import com.hostelhub.model.*;
import com.hostelhub.repository.ComplaintRepository;
import com.hostelhub.repository.GroupMembershipRepository;
import com.hostelhub.repository.HostelGroupRepository;
import com.hostelhub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final HostelGroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;

    public ComplaintService(ComplaintRepository complaintRepository,
                            UserRepository userRepository,
                            HostelGroupRepository groupRepository,
                            GroupMembershipRepository membershipRepository) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
    }

    public ComplaintResponse createComplaint(CreateComplaintRequest request, String userEmail) {
        Complaint complaint = new Complaint();
        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setCategory(request.getCategory());
        complaint.setPriority(request.getPriority() != null ? request.getPriority() : ComplaintPriority.MEDIUM);
        complaint.setRoomNumber(request.getRoomNumber());
        complaint.setStatus(ComplaintStatus.PENDING);

        if (userEmail != null) {
            User student = userRepository.findByEmail(userEmail).orElse(null);
            if (student != null) {
                complaint.setStudentUser(student);
                complaint.setStudentName(student.getName());

                // Find student's hostel group via GroupMembership
                List<GroupMembership> memberships = membershipRepository.findByUserId(student.getId());
                if (!memberships.isEmpty()) {
                    complaint.setHostelGroup(memberships.get(0).getGroup());
                }
            }
        }

        if (complaint.getStudentName() == null) {
            complaint.setStudentName(request.getStudentName() != null ? request.getStudentName() : "Anonymous Student");
        }

        Complaint saved = complaintRepository.save(complaint);
        return mapToResponse(saved);
    }

    // Overload for backward compatibility in tests
    public ComplaintResponse createComplaint(CreateComplaintRequest request) {
        return createComplaint(request, null);
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> getComplaintsForUser(String userEmail,
                                                        ComplaintStatus status,
                                                        ComplaintPriority priority,
                                                        ComplaintCategory category,
                                                        Long groupId) {
        User user = userEmail != null ? userRepository.findByEmail(userEmail).orElse(null) : null;
        List<Complaint> complaints;

        if (user != null && user.getRole() == Role.STUDENT) {
            complaints = complaintRepository.findByStudentUserId(user.getId());
        } else if (user != null && user.getRole() == Role.WARDEN) {
            List<GroupMembership> memberships = membershipRepository.findByUserId(user.getId());
            if (!memberships.isEmpty()) {
                Long wardenGroupId = memberships.get(0).getGroup().getId();
                complaints = complaintRepository.findByHostelGroupId(wardenGroupId);
            } else {
                complaints = complaintRepository.findAll();
            }
        } else {
            complaints = complaintRepository.findAll();
        }

        return complaints.stream()
                .filter(c -> status == null || c.getStatus() == status)
                .filter(c -> priority == null || c.getPriority() == priority)
                .filter(c -> category == null || c.getCategory() == category)
                .filter(c -> groupId == null || (c.getHostelGroup() != null && c.getHostelGroup().getId().equals(groupId)))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> getAllComplaints() {
        return getComplaintsForUser(null, null, null, null, null);
    }

    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintByIdForUser(Long id, String userEmail) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));

        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null && user.getRole() == Role.STUDENT) {
                if (complaint.getStudentUser() != null && !complaint.getStudentUser().getId().equals(user.getId())) {
                    throw new UnauthorizedComplaintAccessException("You do not have permission to view another student's complaint");
                }
            }
        }

        return mapToResponse(complaint);
    }

    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(Long id) {
        return getComplaintByIdForUser(id, null);
    }

    public ComplaintResponse assignComplaint(Long complaintId, Long wardenId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));

        User warden = userRepository.findById(wardenId)
                .orElseThrow(() -> new UserNotFoundException("Warden user not found with ID: " + wardenId));

        if (warden.getRole() != Role.WARDEN && warden.getRole() != Role.ADMIN) {
            throw new InvalidAssignmentException("Assigned user must have role WARDEN or ADMIN");
        }

        complaint.setAssignedWarden(warden);
        if (complaint.getStatus() == ComplaintStatus.PENDING) {
            complaint.setStatus(ComplaintStatus.ASSIGNED);
        }

        Complaint saved = complaintRepository.save(complaint);
        return mapToResponse(saved);
    }

    public ComplaintResponse updateComplaint(Long id, UpdateComplaintRequest request) {
        Complaint existing = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));

        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setCategory(request.getCategory());
        if (request.getPriority() != null) {
            existing.setPriority(request.getPriority());
        }
        existing.setRoomNumber(request.getRoomNumber());
        if (request.getStudentName() != null) {
            existing.setStudentName(request.getStudentName());
        }

        Complaint updated = complaintRepository.save(existing);
        return mapToResponse(updated);
    }

    public ComplaintResponse updateComplaintStatus(Long id, UpdateStatusRequest request) {
        Complaint existing = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));

        ComplaintStatus currentStatus = existing.getStatus();
        ComplaintStatus newStatus = request.getStatus();

        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new InvalidStatusTransitionException("Cannot transition complaint status from " + currentStatus + " to " + newStatus);
        }

        existing.setStatus(newStatus);
        if (newStatus == ComplaintStatus.RESOLVED) {
            existing.setResolvedAt(LocalDateTime.now());
        }

        Complaint updated = complaintRepository.save(existing);
        return mapToResponse(updated);
    }

    public ComplaintResponse updateComplaintPriority(Long id, UpdatePriorityRequest request) {
        Complaint existing = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));

        existing.setPriority(request.getPriority());

        Complaint updated = complaintRepository.save(existing);
        return mapToResponse(updated);
    }

    public void deleteComplaint(Long id) {
        if (!complaintRepository.existsById(id)) {
            throw new ResourceNotFoundException("Complaint not found with ID: " + id);
        }
        complaintRepository.deleteById(id);
    }

    private boolean isValidStatusTransition(ComplaintStatus from, ComplaintStatus to) {
        if (from == to) return true;
        return switch (from) {
            case PENDING -> to == ComplaintStatus.ASSIGNED || to == ComplaintStatus.IN_PROGRESS;
            case ASSIGNED -> to == ComplaintStatus.IN_PROGRESS || to == ComplaintStatus.RESOLVED;
            case IN_PROGRESS -> to == ComplaintStatus.RESOLVED;
            case RESOLVED -> to == ComplaintStatus.CLOSED;
            case CLOSED -> false;
        };
    }

    private ComplaintResponse mapToResponse(Complaint complaint) {
        Long studentId = complaint.getStudentUser() != null ? complaint.getStudentUser().getId() : null;
        String studentEmail = complaint.getStudentUser() != null ? complaint.getStudentUser().getEmail() : null;
        Long groupId = complaint.getHostelGroup() != null ? complaint.getHostelGroup().getId() : null;
        String groupName = complaint.getHostelGroup() != null ? complaint.getHostelGroup().getName() : null;
        String groupSectionCode = complaint.getHostelGroup() != null ? complaint.getHostelGroup().getSectionCode() : null;
        Long wardenId = complaint.getAssignedWarden() != null ? complaint.getAssignedWarden().getId() : null;
        String wardenName = complaint.getAssignedWarden() != null ? complaint.getAssignedWarden().getName() : null;

        return new ComplaintResponse(
                complaint.getId(),
                complaint.getTitle(),
                complaint.getDescription(),
                complaint.getCategory(),
                complaint.getPriority(),
                complaint.getRoomNumber(),
                complaint.getStudentName(),
                studentId,
                studentEmail,
                groupId,
                groupName,
                groupSectionCode,
                wardenId,
                wardenName,
                complaint.getStatus(),
                complaint.getCreatedAt(),
                complaint.getUpdatedAt(),
                complaint.getResolvedAt()
        );
    }
}
