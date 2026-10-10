package com.hostelhub.repository;

import com.hostelhub.model.HostelGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HostelGroupRepository extends JpaRepository<HostelGroup, Long> {
    Optional<HostelGroup> findBySectionCode(String sectionCode);
    boolean existsBySectionCode(String sectionCode);
}
