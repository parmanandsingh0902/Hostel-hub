package com.hostelhub.repository;

import com.hostelhub.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByStudentUserId(Long studentUserId);
    List<Complaint> findByHostelGroupId(Long hostelGroupId);
    List<Complaint> findByAssignedWardenId(Long assignedWardenId);
}
